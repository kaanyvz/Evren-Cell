package com.i2i.evrencell.aom.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.i2i.evrencell.aom.model.Token;
import com.i2i.evrencell.aom.model.User;
import com.i2i.evrencell.aom.repository.CustomerRepository;
import com.i2i.evrencell.aom.repository.TokenRepository;
import com.i2i.evrencell.aom.request.LoginCustomerRequest;
import com.i2i.evrencell.aom.request.RegisterCustomerRequest;
import com.i2i.evrencell.aom.response.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerCustomer_successfulRegistration() throws Exception {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("kaan333@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .password("123")
                .packageName("EVRENCELL MARS")
                .TCNumber("12345678909")
                .msisdn("123456789012")
                .build();
        AuthenticationResponse expectedResponse = new AuthenticationResponse("accessToken", "refreshToken");

        when(customerRepository.createCustomerInOracle(request)).thenReturn(expectedResponse);
        when(customerRepository.createCustomerInVoltDB(request)).thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        AuthenticationResponse response = authService.registerCustomer(request);

        assertEquals(expectedResponse, response);
        verify(customerRepository).createCustomerInOracle(request);
        verify(customerRepository).createCustomerInVoltDB(request);
        verify(tokenRepository, never()).addToken(any());
    }

    @Test
    void registerCustomer_voltDBFailure() throws Exception {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("kaan333@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .password("123")
                .packageName("EVRENCELL MARS")
                .TCNumber("12345678909")
                .msisdn("123456789012")
                .build();

        when(customerRepository.createCustomerInOracle(request)).thenReturn(new AuthenticationResponse("accessToken", "refreshToken"));
        when(customerRepository.createCustomerInVoltDB(request)).thenReturn(new ResponseEntity<>("Failure", HttpStatus.INTERNAL_SERVER_ERROR));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerCustomer(request));

        assertEquals("Failed to create customer in VoltDB for MSISDN: 123456789012", exception.getMessage());
        verify(customerRepository).createCustomerInOracle(request);
        verify(customerRepository).createCustomerInVoltDB(request);
        verify(tokenRepository, never()).addToken(any());
    }

    @Test
    void loginAuth_successfulLogin() throws Exception {
        LoginCustomerRequest request = new LoginCustomerRequest("1234567890", "password");
        User user = new User();
        user.setUserId(1);
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(customerRepository.findByMsisdn(request.msisdn())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        AuthenticationResponse response = authService.loginAuth(request);

        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository).addToken(any(Token.class));
    }

    @Test
    void loginAuth_userNotFound() {
        LoginCustomerRequest request = new LoginCustomerRequest("1234567890", "password");

        when(customerRepository.findByMsisdn(request.msisdn())).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> authService.loginAuth(request));

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository, never()).addToken(any());
    }
}