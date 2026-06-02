package com.finalyear.liwatch.userManagement.controller;


import com.finalyear.liwatch.userManagement.service.JwtService;
import com.finalyear.liwatch.userManagement.service.OAuthUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OauthUserController {

    private final JwtService jwtService;
    private final OAuthUserService oAuthUserService;

    @org.springframework.beans.factory.annotation.Value("${liwatch.frontend.url}")
    private String frontendUrl;

    public OauthUserController(JwtService jwtService, OAuthUserService oAuthUserService) {
        this.jwtService = jwtService;
        this.oAuthUserService = oAuthUserService;
    }

    @GetMapping("/success")
    public void oauth2LoginSuccess(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletResponse response
    ) throws IOException {

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/auth?error=failed");
            return;
        }

        String name = oAuth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = oAuth2User.getAttribute("given_name");
        }
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        try {
            oAuthUserService.createOrUpdateUser(email, name);

            String token = jwtService.generateToken(email);
            String frontendCallbackUrl = frontendUrl + "/auth/callback?token=" + token;
            response.sendRedirect(frontendCallbackUrl);
        } catch (LockedException e) {
            response.sendRedirect(frontendUrl + "/auth?error=suspended");
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth?error=failed");
        }
    }
}
