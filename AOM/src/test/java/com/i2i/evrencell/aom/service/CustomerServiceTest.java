package com.i2i.evrencell.aom.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.i2i.evrencell.aom.dto.CustomerDto;
import com.i2i.evrencell.aom.mapper.CustomerMapper;
import com.i2i.evrencell.aom.model.Customer;
import com.i2i.evrencell.aom.repository.CustomerRepository;
import com.i2i.evrencell.voltdb.VoltCustomer;
import com.i2i.evrencell.voltdb.VoltdbOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private VoltdbOperator voltdbOperator;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCustomerByMsisdn_successful() throws IOException, InterruptedException, ProcCallException {
        String msisdn = "905310270120";
        VoltCustomer voltCustomer = VoltCustomer.builder()
                .customerId(1)
                .msisdn("905310270120")
                .email("kaan123321@hotmail.com")
                .name("kaan")
                .surname("yavuz")
                .sDate(new Date())
                .TCNumber("12345678909")
                .build();
        CustomerDto expectedCustomerDto = CustomerDto.builder()
                .customerId(1)
                .msisdn("905310270120")
                .email("kaan123321@hotmail.com")
                .name("kaan")
                .surname("yavuz")
                .sDate(new Date())
                .TCNumber("12345678909")
                .build();

        when(voltdbOperator.getCustomerByMsisdn(msisdn)).thenReturn(Optional.of(voltCustomer));
        when(customerMapper.voltCustomerBalanceToCustomerDto(any(VoltCustomer.class))).thenReturn(expectedCustomerDto);

        CustomerDto result = customerService.getCustomerByMsisdn(msisdn);

        assertEquals(expectedCustomerDto, result);
        verify(voltdbOperator).getCustomerByMsisdn(msisdn);
        verify(customerMapper).voltCustomerBalanceToCustomerDto(voltCustomer);
    }

    @Test
    void getCustomerByMsisdn_customerNotFound() throws IOException, InterruptedException, ProcCallException {
        String msisdn = "1234567890";

        when(voltdbOperator.getCustomerByMsisdn(msisdn)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> customerService.getCustomerByMsisdn(msisdn));

        assertEquals("Customer not found", exception.getMessage());
        verify(voltdbOperator).getCustomerByMsisdn(msisdn);
        verify(customerMapper, never()).voltCustomerBalanceToCustomerDto(any());
    }

    @Test
    void getAllCustomers_successful() throws SQLException, ClassNotFoundException {
        CustomerDto customerDto = CustomerDto.builder()
                .customerId(1)
                .msisdn("123456789093")
                .email("kaan@gmail.com")
                .name("kaan")
                .surname("yavuz")
                .sDate(new Date())
                .TCNumber("12345678909")
                .build();
        List<CustomerDto> customerDtos = List.of(customerDto);
        when(customerRepository.getAllCustomers()).thenReturn(List.of(new Customer()));
        when(customerMapper.customerToCustomerDto(any())).thenReturn(customerDtos.get(0));

        List<CustomerDto> result = customerService.getAllCustomers();

        assertEquals(customerDtos, result);
        verify(customerRepository).getAllCustomers();
        verify(customerMapper, times(1)).customerToCustomerDto(any());
    }

    @Test
    void getAllCustomers_sqlException() throws SQLException, ClassNotFoundException {
        when(customerRepository.getAllCustomers()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> customerService.getAllCustomers());
        verify(customerRepository).getAllCustomers();
        verify(customerMapper, never()).customerToCustomerDto(any());
    }
}