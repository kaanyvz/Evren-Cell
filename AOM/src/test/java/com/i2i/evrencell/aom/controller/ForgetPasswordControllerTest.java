package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.request.ForgetPasswordRequest;
import com.i2i.evrencell.aom.service.ForgetPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ForgetPasswordControllerTest {

    @Mock
    private ForgetPasswordService forgetPasswordService;

    @InjectMocks
    private ForgetPasswordController forgetPasswordController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void resetPasswordSuccessfully() {
        ForgetPasswordRequest request = ForgetPasswordRequest.builder()
                .email("kaan@gmail.com")
                .TCNumber("12345678909")
                .build();
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Password reset email sent");
        when(forgetPasswordService.forgetPassword(request)).thenReturn(expectedResponse);

        ResponseEntity<ResponseEntity<String>> response = forgetPasswordController.resetPassword(request);

        assertEquals(expectedResponse, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

}