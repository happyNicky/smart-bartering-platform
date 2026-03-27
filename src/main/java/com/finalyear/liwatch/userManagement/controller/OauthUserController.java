package com.finalyear.liwatch.userManagement.controller;


import com.finalyear.liwatch.userManagement.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OauthUserController {

    private final JwtService jwtService;

    public OauthUserController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/success")
    public void oauth2LoginSuccess(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletResponse response
    ) throws IOException {

        String email = oAuth2User.getAttribute("email");
        String token = jwtService.generateToken(email);

        // construct the frontend URL
        String frontendCallbackUrl = "http://localhost:3000/auth/callback?token=" + token;

        // redirect the user's browser back to next.js app
        response.sendRedirect(frontendCallbackUrl);
    }
}
