package com.finalyear.liwatch.userManagement.service;

import com.finalyear.liwatch.userManagement.DTO.LoginUserDto;
import com.finalyear.liwatch.userManagement.DTO.RegisterUserDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    private final  EmailSendingService emailSendingService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, EmailSendingService emailSendingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailSendingService = emailSendingService;
    }

    @Transactional
    public User register(RegisterUserDto user) {

       Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
       if(existingUser.isPresent())
       {
           User use= existingUser.get();
           if(use.isEnabled())
           {   throw new IllegalArgumentException("Email already in use: " + user.getEmail());
           }
           String token = UUID.randomUUID().toString();

           use.setVerificationToken(token);
           use.setTokenExpiry(LocalDateTime.now().plusHours(24));

           userRepository.save(use);
           System.out.println("token: "+token);
           emailSendingService.sendVerificationEmail(user.getEmail(), token);
           return use;

       }


        User u= new User();
        u.setFullName(user.getFullName());
        u.setEmail(user.getEmail());
        u.setPassword(passwordEncoder.encode(user.getPassword()));
        String token = UUID.randomUUID().toString();

        u.setVerificationToken(token);
        u.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(u);
        emailSendingService.sendVerificationEmail(user.getEmail(), token);
        return u;
    }

    public ResponseEntity<? extends Object> verify(LoginUserDto user) {

        Optional<User> existingUser= userRepository.findByEmail(user.getEmail());
        if(existingUser.isPresent () && ! existingUser.get().isEnabled())
        {

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Please verify your email first");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            User u = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "email", user.getEmail()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }


    public ResponseEntity<?> emailVarify(String token) {

        User user =  userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if(user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        System.out.println("email varification being completed!");
        user.setEnabled(true);
        user.setVerificationToken(null);
        System.out.println("varification token set to null");
        user.setTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully");
    }
}

