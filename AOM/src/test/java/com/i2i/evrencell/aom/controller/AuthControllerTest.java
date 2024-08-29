package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.request.LoginCustomerRequest;
import com.i2i.evrencell.aom.request.RegisterCustomerRequest;
import com.i2i.evrencell.aom.response.AuthenticationResponse;
import com.i2i.evrencell.aom.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerCustomerSuccessfully() throws SQLException, ClassNotFoundException, IOException, InterruptedException, ProcCallException {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("kaan333@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .password("123")
                .packageName("EVRENCELL MARS")
                .TCNumber("12345678909")
                .msisdn("123456789012")
                .build();
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5MDUzMTAyNzAxMjAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzI0OTIwNzA3LCJleHAiOjE3MjUwMDcxMDd9.YyDRnmuvtDD_XKcCOfU9Et0AmHAC4v0AsyoYBDtd0NY")
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5MDUzMTAyNzAxMjAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzI0OTIwNzA3LCJleHAiOjE3MjU1MjU1MDd9.gxbYFvKlKulQW0NC6hfUDqxXCTqUFB-BuemLvzFBGRI")
                .build();
        when(authService.registerCustomer(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthenticationResponse> response = authController.registerCustomer(request);

        assertEquals(expectedResponse, response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void registerCustomerWithException() throws SQLException, ClassNotFoundException, IOException, InterruptedException, ProcCallException {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("kaan333@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .password("123")
                .packageName("EVRENCELL MARS")
                .TCNumber("12345678909")
                .msisdn("123456789012")
                .build();
        when(authService.registerCustomer(request)).thenThrow(new SQLException("Database error"));

        try {
            authController.registerCustomer(request);
        } catch (SQLException e) {
            assertEquals("Database error", e.getMessage());
        }
    }

    @Test
    void loginCustomerSuccessfully() throws SQLException, ClassNotFoundException {
        LoginCustomerRequest request = LoginCustomerRequest.builder()
                .msisdn("")
                .password("")
                .build();
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5MDUzMTAyNzAxMjAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzI0OTIwNzM4LCJleHAiOjE3MjUwMDcxMzh9.qMzqDUQkbP9N3gRibJEBDzZsRtY1OHlU2fMYIiY12bQ")
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5MDUzMTAyNzAxMjAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzI0OTIwNzM4LCJleHAiOjE3MjU1MjU1Mzh9.TRUOOFMN-cxhztC9W3MBDNy7deHTd6kUsTjX0lHfL4o")
                .build();
        when(authService.loginAuth(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthenticationResponse> response = authController.login(request);

        assertEquals(expectedResponse, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void loginCustomerWithException() throws SQLException, ClassNotFoundException {
        LoginCustomerRequest request = LoginCustomerRequest.builder()
                .msisdn("")
                .password("")
                .build();
        when(authService.loginAuth(request)).thenThrow(new SQLException("Database error"));

        try {
            authController.login(request);
        } catch (SQLException e) {
            assertEquals("Database error", e.getMessage());
        }
    }
}