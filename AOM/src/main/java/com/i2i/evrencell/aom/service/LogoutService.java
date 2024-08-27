package com.i2i.evrencell.aom.service;

import com.i2i.evrencell.aom.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutHandler {
    private static final Logger logger  = LoggerFactory.getLogger(LogoutService.class);
    private final TokenRepository tokenRepository;

    public LogoutService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        logger.debug("Logging out user");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setRevoked(true);
            storedToken.setExpired(true);
            try {
                tokenRepository.updateTokenStatus(storedToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.debug("User logged out successfully");
    }
}
