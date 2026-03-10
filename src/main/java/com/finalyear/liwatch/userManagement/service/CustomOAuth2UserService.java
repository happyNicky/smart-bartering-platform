package com.finalyear.liwatch.userManagement.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuthUserService oAuthUserService;

    public CustomOAuth2UserService( OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;

    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Extract information from OAuth2 provider
        String email = oAuth2User.getAttribute("email");

        // Map to your user DB (create or update)
        oAuthUserService.createOrUpdateUser(email);

        return oAuth2User;
    }
}