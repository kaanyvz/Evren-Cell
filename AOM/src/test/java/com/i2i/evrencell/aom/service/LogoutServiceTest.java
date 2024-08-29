package com.i2i.evrencell.aom.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.i2i.evrencell.aom.model.Token;
import com.i2i.evrencell.aom.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logout_userSuccessfullyLoggedOut() {
        String jwt = "validToken";
        Token token = new Token();
        token.setToken(jwt);
        token.setRevoked(false);
        token.setExpired(false);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(token));

        logoutService.logout(request, response, authentication);

        assertTrue(token.isRevoked());
        assertTrue(token.isExpired());
        verify(tokenRepository).updateTokenStatus(token);
    }

    @Test
    void logout_noAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
        verify(tokenRepository, never()).updateTokenStatus(any(Token.class));
    }

    @Test
    void logout_invalidAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
        verify(tokenRepository, never()).updateTokenStatus(any(Token.class));
    }

    @Test
    void logout_tokenNotFound() {
        String jwt = "validToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.empty());

        logoutService.logout(request, response, authentication);

        verify(tokenRepository).findByToken(jwt);
        verify(tokenRepository, never()).updateTokenStatus(any(Token.class));
    }

    @Test
    void logout_exceptionWhileUpdatingTokenStatus() {
        String jwt = "validToken";
        Token token = new Token();
        token.setToken(jwt);
        token.setRevoked(false);
        token.setExpired(false);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(token));
        doThrow(new RuntimeException("Database error")).when(tokenRepository).updateTokenStatus(token);

        logoutService.logout(request, response, authentication);

        assertTrue(token.isRevoked());
        assertTrue(token.isExpired());
        verify(tokenRepository).updateTokenStatus(token);
    }
}