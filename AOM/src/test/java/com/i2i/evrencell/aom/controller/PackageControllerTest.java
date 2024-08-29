package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.dto.PackageDetails;
import com.i2i.evrencell.aom.dto.PackageDto;
import com.i2i.evrencell.aom.service.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PackageControllerTest {

    @Mock
    private PackageService packageService;

    @InjectMocks
    private PackageController packageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllPackages_returnsListOfPackages() {
        PackageDto packageDto = PackageDto.builder().build();
        List<PackageDto> packages = List.of(packageDto);
        when(packageService.getAllPackages()).thenReturn(packages);

        ResponseEntity<List<PackageDto>> response = packageController.getAllPackages();

        assertEquals(packages, response.getBody());
    }

    @Test
    void getAllPackages_returnsEmptyList() {
        when(packageService.getAllPackages()).thenReturn(Collections.emptyList());

        ResponseEntity<List<PackageDto>> response = packageController.getAllPackages();

        assertEquals(Collections.emptyList(), response.getBody());
    }

    @Test
    void getUserPackageByMsisdn_returnsPackage() {
        String msisdn = "1234567890";
        PackageDto packageDto = PackageDto.builder().build();
        when(packageService.getUserPackageByMsisdn(msisdn)).thenReturn(packageDto);

        ResponseEntity<PackageDto> response = packageController.getUserPackageByMsisdn(msisdn);

        assertEquals(packageDto, response.getBody());
    }

    @Test
    void getUserPackageByMsisdn_returnsNull() {
        String msisdn = "1234567890";
        when(packageService.getUserPackageByMsisdn(msisdn)).thenReturn(null);

        ResponseEntity<PackageDto> response = packageController.getUserPackageByMsisdn(msisdn);

        assertEquals(null, response.getBody());
    }

    @Test
    void getPackageDetails_returnsPackageDetails() {
        String packageName = "TestPackage";
        PackageDetails _packageDetails = PackageDetails.builder().build();
        Optional<PackageDetails> packageDetails = Optional.of(_packageDetails);
        when(packageService.getPackageDetails(packageName)).thenReturn(packageDetails);

        ResponseEntity<Optional<PackageDetails>> response = packageController.getPackageDetails(packageName);

        assertEquals(packageDetails, response.getBody());
    }

    @Test
    void getPackageDetails_returnsEmpty() {
        String packageName = "TestPackage";
        when(packageService.getPackageDetails(packageName)).thenReturn(Optional.empty());

        ResponseEntity<Optional<PackageDetails>> response = packageController.getPackageDetails(packageName);

        assertEquals(Optional.empty(), response.getBody());
    }
}