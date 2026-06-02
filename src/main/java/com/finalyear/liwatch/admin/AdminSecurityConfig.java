package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.userManagement.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public AdminSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            // apply this chain only to admin paths
            .securityMatcher("/api/admin/**")
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

