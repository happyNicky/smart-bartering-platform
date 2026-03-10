package com.finalyear.liwatch.userManagement.controller;

import com.finalyear.liwatch.userManagement.DTO.LoginUserDto;
import com.finalyear.liwatch.userManagement.DTO.RegisterUserDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController()
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
        try {
            User user = userService.register(dto);
            return ResponseEntity.ok("User registered successfully: " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto user) {
        return userService.verify(user);
    }
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        return userService.emailVarify(token);
    }

}
