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
            // Check if this was an OAuth-only user (has placeholder password)
            if (passwordEncoder.matches("oauth2user", u.getPassword())) {
                // The user originally signed up via Google but now wants to set a password and sign up standard
                u.setPassword(passwordEncoder.encode(dto.getPassword()));
                if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
                    u.setFullName(dto.getFullName());
                }
                u.setEnabled(true);
                u.setVerified(true);
                if (u.getUserProfile() == null) {
                    UserProfile profile = new UserProfile();
                    profile.setUser(u);
                    profile.setLocation("");
                    u.setUserProfile(profile);
                }
                return userRepository.save(u);
            }
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
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getStatus() == com.finalyear.liwatch.userManagement.utils.enums.Status.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account has been suspended.");
            }
            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email first");
            }
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

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean isOauthOnly = passwordEncoder.matches("oauth2user", user.getPassword());
        if (!isOauthOnly) {
            if (currentPassword == null || currentPassword.isBlank() ||
                    !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password");
            }
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
