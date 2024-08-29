package com.i2i.evrencell.aom.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.i2i.evrencell.aom.encryption.CustomerPasswordEncoder;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.repository.CustomerRepository;
import com.i2i.evrencell.aom.request.ForgetPasswordRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class ForgetPasswordServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerPasswordEncoder customerPasswordEncoder;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private OracleConnection oracleConnection;

    @InjectMocks
    private ForgetPasswordService forgetPasswordService;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        MockitoAnnotations.openMocks(this);
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        Connection mockConnection = mock(Connection.class);
        when(oracleConnection.getOracleConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareCall(anyString())).thenReturn(mock(CallableStatement.class));
    }

    @Test
    void forgetPassword_successfulReset() throws Exception {
        ForgetPasswordRequest request = new ForgetPasswordRequest("kaan333@gmail.com", "12345678909");
        when(customerRepository.checkCustomerExists(request.email(), request.TCNumber())).thenReturn(true);
        when(customerPasswordEncoder.encrypt(anyString())).thenReturn("encryptedPassword");

        ResponseEntity<String> response = forgetPasswordService.forgetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Please check your mail address.", response.getBody());
        verify(customerRepository).checkCustomerExists(request.email(), request.TCNumber());
        verify(customerPasswordEncoder).encrypt(anyString());
        verify(javaMailSender).send(any(MimeMessage.class));
        verify(customerRepository).updatePasswordInOracle(request.email(), request.TCNumber(), "encryptedPassword");
        verify(customerRepository).updatePasswordInVoltDB(request.email(), request.TCNumber(), "encryptedPassword");
    }

    @Test
    void forgetPassword_customerNotFound() throws Exception {
        ForgetPasswordRequest request = new ForgetPasswordRequest("kaan333@gmail.com", "12345678909");
        when(customerRepository.checkCustomerExists(request.email(), request.TCNumber())).thenReturn(false);

        ResponseEntity<String> response = forgetPasswordService.forgetPassword(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Customer does not exist", response.getBody());
        verify(customerRepository).checkCustomerExists(request.email(), request.TCNumber());
        verify(customerPasswordEncoder, never()).encrypt(anyString());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
        verify(customerRepository, never()).updatePasswordInOracle(anyString(), anyString(), anyString());
        verify(customerRepository, never()).updatePasswordInVoltDB(anyString(), anyString(), anyString());
    }

    @Test
    void forgetPassword_updatePasswordFailed() throws Exception {
        ForgetPasswordRequest request = new ForgetPasswordRequest("kaan333@gmail.com", "12345678909");
        when(customerRepository.checkCustomerExists(request.email(), request.TCNumber())).thenReturn(true);
        when(customerPasswordEncoder.encrypt(anyString())).thenReturn("encryptedPassword");
        doThrow(SQLException.class).when(customerRepository).updatePasswordInOracle(anyString(), anyString(), anyString());

        ResponseEntity<String> response = forgetPasswordService.forgetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error occurred while resetting password", response.getBody());
        verify(customerRepository).checkCustomerExists(request.email(), request.TCNumber());
        verify(customerPasswordEncoder).encrypt(anyString());
        verify(javaMailSender).send(any(MimeMessage.class));
        verify(customerRepository).updatePasswordInOracle(anyString(), anyString(), anyString());
        verify(customerRepository, never()).updatePasswordInVoltDB(anyString(), anyString(), anyString());
    }
}