package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.dto.CustomerDto;
import com.i2i.evrencell.aom.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCustomerByMsisdnSuccessfully() throws IOException, InterruptedException, ProcCallException {
        String msisdn = "123456789012";
        CustomerDto expectedCustomer = CustomerDto.builder()
                .customerId(1)
                .msisdn("123456789093")
                .email("kaan@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .sDate(new Date())
                .TCNumber("12345678909")
                .build();
        when(customerService.getCustomerByMsisdn(msisdn)).thenReturn(expectedCustomer);

        ResponseEntity<CustomerDto> response = customerController.getCustomerByMsisdn(msisdn);

        assertEquals(expectedCustomer, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getCustomerByMsisdnWithException() throws IOException, InterruptedException, ProcCallException {
        String msisdn = "123456789012";
        when(customerService.getCustomerByMsisdn(msisdn)).thenThrow(new IOException("IO error"));

        try {
            customerController.getCustomerByMsisdn(msisdn);
        } catch (IOException e) {
            assertEquals("IO error", e.getMessage());
        }
    }

    @Test
    void getAllCustomersSuccessfully() throws SQLException, ClassNotFoundException {
        CustomerDto expectedCustomer = CustomerDto.builder()
                .customerId(1)
                .msisdn("123456789093")
                .email("kaan@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .sDate(new Date())
                .TCNumber("12345678909")
                .build();
        List<CustomerDto> expectedCustomers = Collections.singletonList(expectedCustomer);
        when(customerService.getAllCustomers()).thenReturn(expectedCustomers);

        ResponseEntity<List<CustomerDto>> response = customerController.getAllCustomers();

        assertEquals(expectedCustomers, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getAllCustomersWithException() throws SQLException, ClassNotFoundException {
        when(customerService.getAllCustomers()).thenThrow(new SQLException("Database error"));

        try {
            customerController.getAllCustomers();
        } catch (SQLException e) {
            assertEquals("Database error", e.getMessage());
        }
    }
}