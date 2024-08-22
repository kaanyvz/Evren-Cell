package com.i2i.evrencell.aom.configuration;

import com.i2i.evrencell.aom.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.i2i.evrencell.aom.enumeration.Role.ADMIN;
import static com.i2i.evrencell.aom.enumeration.Role.USER;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private static final String[] AUTH_WHITELIST = {
            "/v1/api/auth/login",
            "/v1/api/auth/register",
            "/v1/api/forgetPassword/reset",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/api/v1/**",
            "/metrics/**"
    };
    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 AuthenticationProvider authenticationProvider,
                                 LogoutHandler logoutHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.logoutHandler = logoutHandler;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(AUTH_WHITELIST)
                                .permitAll()
                                .requestMatchers("/v1/api/customer/getAllCustomers")
                                    .hasAnyRole(ADMIN.name())
                                .requestMatchers("/v1/api/customer/getCustomerByMsisdn")
                                    .hasAnyRole(USER.name(), ADMIN.name())
                                .requestMatchers("/v1/api/balance/remainingBalance")
                                    .hasAnyRole(USER.name(), ADMIN.name())
                                .requestMatchers("/v1/api/packages/getAllPackages")
                                    .hasAnyRole(USER.name(), ADMIN.name())
                                .requestMatchers("/v1/api/packages/getPackageDetails")
                                    .hasAnyRole(USER.name(), ADMIN.name())
                                .requestMatchers("/v1/api/packages/getUserPackageByMsisdn")
                                    .hasAnyRole(USER.name(), ADMIN.name())
                                .anyRequest()
                                .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout
                        .logoutUrl("/v1/api/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((req, res, auth) -> SecurityContextHolder.clearContext())
                )
                .build();
    }
}
