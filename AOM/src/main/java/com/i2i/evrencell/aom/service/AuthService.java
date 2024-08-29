package com.i2i.evrencell.aom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i2i.evrencell.aom.enumeration.TokenType;
import com.i2i.evrencell.aom.model.Token;
import com.i2i.evrencell.aom.model.User;
import com.i2i.evrencell.aom.repository.CustomerRepository;
import com.i2i.evrencell.aom.repository.TokenRepository;
import com.i2i.evrencell.aom.request.LoginCustomerRequest;
import com.i2i.evrencell.aom.request.RegisterCustomerRequest;
import com.i2i.evrencell.aom.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.i2i.hazelcast.utils.HazelcastMWOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(CustomerRepository customerRepository,
                       TokenRepository tokenRepository,
                       JWTService jwtService,
                       AuthenticationManager authenticationManager) {
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    public AuthenticationResponse registerCustomer(RegisterCustomerRequest registerCustomerRequest) throws SQLException,
            ClassNotFoundException, IOException, ProcCallException, InterruptedException {

        logger.debug("Starting to register customer in Oracle and VoltDB with MSISDN: " + registerCustomerRequest.msisdn());
        AuthenticationResponse authenticationResponse = customerRepository.createCustomerInOracle(registerCustomerRequest);

        ResponseEntity<String> voltResponse = customerRepository.createCustomerInVoltDB(registerCustomerRequest);
        if (!voltResponse.getStatusCode().is2xxSuccessful()) {
            logger.error("Failed to create customer in VoltDB for MSISDN: " + registerCustomerRequest.msisdn());
            throw new RuntimeException("Failed to create customer in VoltDB for MSISDN: " + registerCustomerRequest.msisdn());
        }

        HazelcastMWOperation.put(registerCustomerRequest.msisdn(), registerCustomerRequest.msisdn());
        logger.debug("Successfully registered customer in Oracle, VoltDB, and Hazelcast for MSISDN: " + registerCustomerRequest.msisdn());

        return authenticationResponse;
    }

    public AuthenticationResponse loginAuth(LoginCustomerRequest request) throws SQLException, ClassNotFoundException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.msisdn(), request.password()));
        var user = customerRepository.findByMsisdn(request.msisdn())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws SQLException, ClassNotFoundException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String msisdn;
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return;
        }
        refreshToken = authHeader.substring(7);
        msisdn = jwtService.extractMsisdnFromToken(refreshToken);
        if(msisdn != null){
            var user = customerRepository.findByMsisdn(msisdn)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if(jwtService.isTokenValid(refreshToken, user)){
                var newAccessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, newAccessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private void revokeAllUserTokens(User user) throws SQLException, ClassNotFoundException {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getUserId());
        if(validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
            tokenRepository.updateTokenStatus(token);
        });
    }

    private void saveUserToken(User user, String jwtToken) {

        var token = Token
                .builder()
                .userId(user.getUserId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.addToken(token);
    }
}












