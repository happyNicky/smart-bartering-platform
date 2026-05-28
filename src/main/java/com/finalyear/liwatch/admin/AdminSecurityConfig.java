package com.finalyear.liwatch.admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Dedicated security filter chain for /api/admin/** endpoints.
 *
 * This is layered on top of your existing app-wide SecurityFilterChain
 * (give it a lower @Order number so it runs first for admin paths).
 *
 * Two-layer protection:
 *   1. URL-level  → only ADMIN role may reach /api/admin/**
 *   2. Method-level → @AdminOnly annotation on each controller method
 *
 * If your project already has a central SecurityConfig, you can merge
 * the requestMatcher block there instead of creating a second chain.
 */
@Configuration
@EnableMethodSecurity          // enables @PreAuthorize / @AdminOnly
@Order(1)                      // run before the main app filter chain
public class AdminSecurityConfig {

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            // apply this chain only to admin paths
            .securityMatcher("/api/admin/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("ADMIN")
            )
            // reuse your existing JWT filter — no form login for admin API
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
