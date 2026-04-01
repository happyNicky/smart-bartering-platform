package com.finalyear.liwatch.userManagement.utils.classes;

import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtilMethods {

    private final UserRepository userRepository;

    public UserUtilMethods(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public  User getCurrentlyAuthenticatedUser()
    {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String username = authentication.getName();

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
