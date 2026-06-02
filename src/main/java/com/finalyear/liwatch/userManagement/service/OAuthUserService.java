package com.finalyear.liwatch.userManagement.service;

import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userprofile.UserProfile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;


@Service
public class OAuthUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public OAuthUserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createOrUpdateUser(String email, String name) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == com.finalyear.liwatch.userManagement.utils.enums.Status.SUSPENDED) {
                throw new org.springframework.security.authentication.LockedException("Your account has been suspended.");
            }
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                user.setFullName(name);
            }
            // Ensure they have a user profile if they don't have one yet
            if (user.getUserProfile() == null) {
                UserProfile profile = new UserProfile();
                profile.setUser(user);
                profile.setLocation("");
                user.setUserProfile(profile);
            }
            userRepository.save(user);
        });
        
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPassword(passwordEncoder.encode("oauth2user"));
            user.setEnabled(true);
            user.setVerified(true);
            
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setLocation("");
            user.setUserProfile(profile);
            
            userRepository.save(user);
        }
    }
}