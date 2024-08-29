package com.i2i.evrencell.aom.service;

import com.i2i.evrencell.aom.dto.PackageDetails;
import com.i2i.evrencell.aom.dto.PackageDto;
import com.i2i.evrencell.aom.mapper.PackageMapper;
import com.i2i.evrencell.aom.model.Package;
import com.i2i.evrencell.aom.repository.PackageRepository;
import com.i2i.evrencell.voltdb.VoltPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PackageMapper packageMapper;

    @InjectMocks
    private PackageService packageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllPackagesReturnsListOfPackageDtos() throws SQLException, ClassNotFoundException {
        List<Package> packages = List.of(new Package());
        PackageDto packageDto = PackageDto.builder()
                .packageId(1)
                .packageName("EVRENCELL MARS")
                .amountMinutes(100)
                .amountSms(150)
                .amountData(512)
                .period(30)
                .build();
        List<PackageDto> packageDtos = List.of(packageDto);

        when(packageRepository.getAllPackages()).thenReturn(packages);
        when(packageMapper.packageToPackageDto(any(Package.class))).thenReturn(packageDtos.get(0));

        List<PackageDto> result = packageService.getAllPackages();

        assertEquals(packageDtos, result);
    }

    @Test
    void getAllPackagesThrowsRuntimeExceptionOnSQLException() throws SQLException, ClassNotFoundException {
        when(packageRepository.getAllPackages()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> packageService.getAllPackages());
    }

    @Test
    void getUserPackageByMsisdnReturnsPackageDto() throws IOException, ProcCallException {
        String msisdn = "1234567890";
        VoltPackage voltPackage = new VoltPackage
                (1, "EVRENCELL MARS", 9.99, 100, 1024, 50, 30);

        PackageDto packageDto = PackageDto.builder()
                .packageId(1)
                .packageName("EVRENCELL MARS")
                .amountMinutes(100)
                .amountSms(150)
                .amountData(512)
                .period(30)
                .build();

        when(packageRepository.getUserPackageByMsisdn(msisdn)).thenReturn(voltPackage);
        when(packageMapper.voltPackageToPackageDto(voltPackage)).thenReturn(packageDto);

        PackageDto result = packageService.getUserPackageByMsisdn(msisdn);

        assertEquals(packageDto, result);
    }

    @Test
    void getUserPackageByMsisdnThrowsRuntimeExceptionOnIOException() throws IOException, ProcCallException {
        String msisdn = "1234567890";

        when(packageRepository.getUserPackageByMsisdn(msisdn)).thenThrow(IOException.class);

        assertThrows(RuntimeException.class, () -> packageService.getUserPackageByMsisdn(msisdn));
    }

    @Test
    void getPackageDetailsReturnsPackageDetails() throws SQLException, ClassNotFoundException {
        String packageName = "TestPackage";
        PackageDetails packageDetails = PackageDetails.builder()
                .packageName("EVRENCELL MARS")
                .amountData(512)
                .amountMinutes(100)
                .amountSms(150)
                .build();

        when(packageRepository.getPackageDetails(packageName)).thenReturn(Optional.of(packageDetails));

        Optional<PackageDetails> result = packageService.getPackageDetails(packageName);

        assertTrue(result.isPresent());
        assertEquals(packageDetails, result.get());
    }

    @Test
    void getPackageDetailsThrowsRuntimeExceptionOnSQLException() throws SQLException, ClassNotFoundException {
        String packageName = "TestPackage";

        when(packageRepository.getPackageDetails(packageName)).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> packageService.getPackageDetails(packageName));
    }
}