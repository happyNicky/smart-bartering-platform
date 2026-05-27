package com.finalyear.liwatch.userManagement.service;

import com.finalyear.liwatch.userManagement.DTO.LoginUserDto;
import com.finalyear.liwatch.userManagement.DTO.RegisterUserDto;
import com.finalyear.liwatch.userManagement.model.PasswordResetToken;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.PasswordResetTokenRepository;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userprofile.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailSendingService emailSendingService;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailSendingService emailSendingService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailSendingService = emailSendingService;
    }

    @Transactional
    public User register(RegisterUserDto dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            User u = existing.get();
            if (u.isEnabled()) {
                throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
            }
            String token = UUID.randomUUID().toString();
            u.setVerificationToken(token);
            u.setTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(u);
            emailSendingService.sendVerificationEmail(dto.getEmail(), token);
            return u;
        }

        User u = new User();
        u.setFullName(dto.getFullName());
        u.setEmail(dto.getEmail());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setCreatedAt(LocalDateTime.now());
        String token = UUID.randomUUID().toString();
        u.setVerificationToken(token);
        u.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(u);
        emailSendingService.sendVerificationEmail(dto.getEmail(), token);
        return u;
    }

    public ResponseEntity<?> verify(LoginUserDto dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent() && !existing.get().isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email first");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
            userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String token = jwtService.generateToken(dto);
            return ResponseEntity.ok(Map.of("token", token, "email", dto.getEmail()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @Transactional
    public ResponseEntity<?> emailVarify(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setLocation("");
        user.setUserProfile(profile);
        userRepository.save(user);
        return ResponseEntity.ok("Email verified successfully");
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expired");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired())
                .orElse(false);
    }

    public void createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(resetToken);
        emailSendingService.sendPasswordResetEmail(user.getEmail(), token);
    }
}
