package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.service.BalanceService;
import com.i2i.evrencell.voltdb.VoltCustomerBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


class BalanceControllerTest {

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private BalanceController balanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRemainingCustomerBalanceByMsisdn() throws IOException, InterruptedException, ProcCallException {
        String msisdn = "1234567890";
        VoltCustomerBalance expectedBalance = new VoltCustomerBalance(
                msisdn,
                5120,
                1000,
                1500,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis())
        );
        when(balanceService.getRemainingCustomerBalance(msisdn)).thenReturn(expectedBalance);

        ResponseEntity<VoltCustomerBalance> response = balanceController.getRemainingCustomerBalanceByMsisdn(msisdn);

        assertEquals(expectedBalance, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

}