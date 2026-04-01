package com.finalyear.liwatch.userManagement.config;

import com.finalyear.liwatch.userManagement.service.CustomOAuth2UserService;
import com.finalyear.liwatch.userManagement.service.CustomeUserDetailService;
import com.finalyear.liwatch.userManagement.service.OAuthUserService;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    private final CustomeUserDetailService userDetailService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;




    public WebSecurityConfig(CustomeUserDetailService userDetailService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailService = userDetailService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;


    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,CustomOAuth2UserService customOAuth2UserService) throws Exception {
        httpSecurity
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/verify","/error").permitAll()
                                .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/api/auth/login")
                        .defaultSuccessUrl("/api/auth/oauth2/success", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // inject the service
                        )
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return httpSecurity.build();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(14);
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailService);
        // provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // important!
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws  Exception
    {
        return configuration.getAuthenticationManager();
    }


}
