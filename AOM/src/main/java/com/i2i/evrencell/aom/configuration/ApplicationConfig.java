package com.i2i.evrencell.aom.configuration;

import com.i2i.evrencell.aom.encryption.CustomerPasswordEncoder;
import com.i2i.evrencell.aom.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {
    private final CustomerRepository customerRepository;
    private final CustomerPasswordEncoder passwordEncoder;

    public ApplicationConfig(CustomerRepository customerRepository,
                             CustomerPasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService  userDetailsService() {
        return msisdn -> customerRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with msisdn: " + msisdn));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return passwordEncoder;
    }
}
