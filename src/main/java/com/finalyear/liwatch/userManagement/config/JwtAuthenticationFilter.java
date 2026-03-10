package com.finalyear.liwatch.userManagement.config;

import com.finalyear.liwatch.userManagement.service.CustomeUserDetailService;
import com.finalyear.liwatch.userManagement.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private  final CustomeUserDetailService userDetailService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomeUserDetailService userDetailService) {
        this.jwtService = jwtService;

        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUserName(jwt);
        } catch (Exception e) {
            System.out.println("email not found!");
            filterChain.doFilter(request, response);
            return;
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (username != null && authentication == null) {
            System.out.println("email is not null and authentication is not null");
            //authenticate
//            UserDetails userDetails = userDetailService.loadUserByUsername(username);
            UserDetails userDetails= userDetailService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("jwt token is valid");
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
