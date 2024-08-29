package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.exception.NotFoundException;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.request.CreateBalanceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BalanceRepositoryTest {

    @Mock
    private OracleConnection oracleConnection;

    @InjectMocks
    private BalanceRepository balanceRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOracleBalance_Success() throws SQLException, ClassNotFoundException {
        CreateBalanceRequest request = new CreateBalanceRequest(1, 1, 1);
        Connection connection = mock(Connection.class);
        CallableStatement packageStmt = mock(CallableStatement.class);
        CallableStatement balanceStmt = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call SELECT_PACKAGE_DETAILS_ID(?, ?, ?, ?, ?)}")).thenReturn(packageStmt);
        when(connection.prepareCall("{call INSERT_BALANCE_TO_CUSTOMER(?, ?, ?, ?, ?, ?, ?)}")).thenReturn(balanceStmt);

        doNothing().when(packageStmt).setInt(1, request.packageId());
        when(packageStmt.getInt(2)).thenReturn(100);
        when(packageStmt.getInt(3)).thenReturn(200);
        when(packageStmt.getInt(4)).thenReturn(300);
        when(packageStmt.getInt(5)).thenReturn(30);

        ResponseEntity<String> response = balanceRepository.createOracleBalance(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Balance created successfully", response.getBody());

        verify(packageStmt).execute();
        verify(balanceStmt).execute();
        verify(connection).close();
    }

    @Test
    public void testCreateOracleBalance_PackageNotFound() throws SQLException, ClassNotFoundException {
        CreateBalanceRequest request = new CreateBalanceRequest(1, 1, 1);
        Connection connection = mock(Connection.class);
        CallableStatement packageStmt = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call SELECT_PACKAGE_DETAILS_ID(?, ?, ?, ?, ?)}")).thenReturn(packageStmt);

        doNothing().when(packageStmt).setInt(1, request.packageId());
        when(packageStmt.getInt(2)).thenReturn(0);
        when(packageStmt.getInt(3)).thenReturn(0);
        when(packageStmt.getInt(4)).thenReturn(0);
        when(packageStmt.getInt(5)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> balanceRepository.createOracleBalance(request));

        verify(packageStmt).execute();
        verify(connection).close();
    }

}