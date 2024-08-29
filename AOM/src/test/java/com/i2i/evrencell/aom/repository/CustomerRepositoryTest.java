package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.constant.OracleQueries;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.model.Customer;
import com.i2i.evrencell.aom.request.RegisterCustomerRequest;
import oracle.jdbc.OracleTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CustomerRepositoryTest {

    @Mock
    private OracleConnection oracleConnection;

    @InjectMocks
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllCustomers_Success() throws SQLException, ClassNotFoundException {
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call get_all_customers(?)}")).thenReturn(callableStatement);
        doNothing().when(callableStatement).registerOutParameter(1, OracleTypes.CURSOR);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getObject(1)).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("CUST_ID")).thenReturn(1);
        when(resultSet.getString("MSISDN")).thenReturn("1234567890");
        when(resultSet.getString("NAME")).thenReturn("kaan");
        when(resultSet.getString("SURNAME")).thenReturn("yavuz");
        when(resultSet.getString("EMAIL")).thenReturn("kaan@gmail.com");
        when(resultSet.getTimestamp("SDATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getString("TC_NO")).thenReturn("12345678901");

        List<Customer> customers = customerRepository.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("kaan", customers.get(0).getName());

        verify(resultSet).close();
        verify(callableStatement).close();
        verify(connection).close();
    }


    @Test
    public void testCreateCustomerInOracle_CustomerExists() throws SQLException, ClassNotFoundException {
        RegisterCustomerRequest request =
                new RegisterCustomerRequest("kaan", "yavuz", "1234567890",
                        "kaan@gmail.com", "password", "12345678901",
                        "EVRENCELL MARS");
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareStatement(OracleQueries.IS_CUSTOMER_ALREADY_EXISTS)).thenReturn(stmt);
        doNothing().when(stmt).setString(anyInt(), anyString());
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        assertThrows(RuntimeException.class, () -> customerRepository.createCustomerInOracle(request));

        verify(stmt).close();
        verify(rs).close();
        verify(connection).close();
    }

    @Test
    public void testUpdatePasswordInOracle_Success() throws SQLException, ClassNotFoundException {
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call UPDATE_CUSTOMER_PASSWORD(?, ?, ?)}")).thenReturn(callableStatement);
        doNothing().when(callableStatement).setString(anyInt(), anyString());
        when(callableStatement.execute()).thenReturn(true);

        customerRepository.updatePasswordInOracle("kaan@gmail.com", "12345678901", "encryptedPassword");

        verify(callableStatement).close();
        verify(connection).close();
    }

    @Test
    public void testCheckCustomerExists_Success() throws SQLException, ClassNotFoundException {
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call CHECK_CUSTOMER_EXISTS_BY_MAIL_AND_TCNO(?, ?, ?)}")).thenReturn(callableStatement);
        doNothing().when(callableStatement).setString(anyInt(), anyString());
        doNothing().when(callableStatement).registerOutParameter(3, Types.INTEGER);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getInt(3)).thenReturn(1);

        boolean exists = customerRepository.checkCustomerExists("kaan@gmail.com", "12345678901");

        assertTrue(exists);

        verify(callableStatement).close();
        verify(connection).close();
    }
}