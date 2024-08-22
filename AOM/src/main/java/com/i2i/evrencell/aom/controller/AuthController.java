package com.i2i.evrencell.aom.controller;

import com.i2i.evrencell.aom.request.LoginCustomerRequest;
import com.i2i.evrencell.aom.request.RegisterCustomerRequest;
import com.i2i.evrencell.aom.response.AuthenticationResponse;
import com.i2i.evrencell.aom.service.AuthService;
import com.i2i.evrencell.aom.service.LogoutService;
import jakarta.validation.Valid;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.SQLException;


/**
 * Controller class for Authentication related operations
 */
@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {
    private final AuthService authService;
    private final LogoutService logoutService;
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    public AuthController(AuthService authService,
                          LogoutService logoutService) {
        this.authService = authService;
        this.logoutService = logoutService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest registerCustomerRequest)
            throws SQLException, ClassNotFoundException, IOException, InterruptedException, ProcCallException {
        logger.debug("Request is taken, registering customer");
        return ResponseEntity.ok(authService.registerCustomer(registerCustomerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginCustomerRequest loginCustomerRequest) throws SQLException, ClassNotFoundException {
        logger.debug("Request is taken, logging in customer");
        return ResponseEntity.ok(authService.loginAuth(loginCustomerRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String token) {
        logger.debug("Request is taken, logging out customer");
        logoutService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }


}

