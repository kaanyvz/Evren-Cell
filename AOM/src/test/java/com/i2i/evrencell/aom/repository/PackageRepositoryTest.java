package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.dto.PackageDetails;
import com.i2i.evrencell.aom.exception.NotFoundException;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.model.Package;
import com.i2i.evrencell.voltdb.VoltPackage;
import com.i2i.evrencell.voltdb.VoltdbOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PackageRepositoryTest {

    @Mock
    private OracleConnection oracleConnection;

    @Mock
    private VoltdbOperator voltdbOperator;

    @InjectMocks
    private PackageRepository packageRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllPackages_Success() throws SQLException, ClassNotFoundException {
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call SELECT_ALL_PACKAGES(?)}")).thenReturn(callableStatement);
        when(callableStatement.getObject(1)).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("PACKAGE_ID")).thenReturn(1);
        when(resultSet.getString("PACKAGE_NAME")).thenReturn("Test Package");
        when(resultSet.getInt("AMOUNT_MINUTES")).thenReturn(100);
        when(resultSet.getInt("AMOUNT_DATA")).thenReturn(200);
        when(resultSet.getInt("AMOUNT_SMS")).thenReturn(300);
        when(resultSet.getDouble("PRICE")).thenReturn(9.99);
        when(resultSet.getInt("PERIOD")).thenReturn(30);

        List<Package> packages = packageRepository.getAllPackages();

        assertEquals(1, packages.size());
        assertEquals("Test Package", packages.get(0).getPackageName());

        verify(callableStatement).execute();
        verify(resultSet).close();
        verify(callableStatement).close();
        verify(connection).close();
    }
    @Test
    public void testGetUserPackageByMsisdn_Success() throws IOException, ProcCallException {
        String msisdn = "5552345678";
        VoltPackage mockPackage = new VoltPackage
                (1, "EVRENCELL MARS", 9.99, 100, 1024, 50, 30);

        when(voltdbOperator.getPackageByMsisdn(msisdn)).thenReturn(mockPackage);

        VoltPackage result = packageRepository.getUserPackageByMsisdn(msisdn);

        assertNotNull(result);
        assertEquals("EVRENCELL MARS", result.getPackageName());
        assertEquals(100, result.getPrice(), 0.01);
    }

    @Test
    public void testGetPackageDetails_Success() throws SQLException, ClassNotFoundException {
        String packageName = "Test Package";
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call SELECT_PACKAGE_DETAILS_NAME(?, ?, ?, ?)}")).thenReturn(callableStatement);

        doNothing().when(callableStatement).setString(1, packageName);
        when(callableStatement.getInt(2)).thenReturn(100);
        when(callableStatement.getInt(3)).thenReturn(200);
        when(callableStatement.getInt(4)).thenReturn(300);

        Optional<PackageDetails> packageDetails = packageRepository.getPackageDetails(packageName);

        assertTrue(packageDetails.isPresent());
        assertEquals(100, packageDetails.get().amountMinutes());
        assertEquals(200, packageDetails.get().amountSms());
        assertEquals(300, packageDetails.get().amountData());

        verify(callableStatement).execute();
        verify(callableStatement).close();
        verify(connection).close();
    }

    @Test
    public void testGetPackageDetails_NotFound() throws SQLException, ClassNotFoundException {
        String packageName = "Nonexistent Package";
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call SELECT_PACKAGE_DETAILS_NAME(?, ?, ?, ?)}")).thenReturn(callableStatement);

        doNothing().when(callableStatement).setString(1, packageName);
        when(callableStatement.getInt(2)).thenReturn(0);
        when(callableStatement.getInt(3)).thenReturn(0);
        when(callableStatement.getInt(4)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> packageRepository.getPackageDetails(packageName));

        verify(callableStatement).execute();
        verify(callableStatement).close();
        verify(connection).close();
    }
}