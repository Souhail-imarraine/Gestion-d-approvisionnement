package com.tricol.stock.controller;

import com.tricol.stock.dto.request.LoginRequest;
import com.tricol.stock.dto.request.RefreshTokenRequest;
import com.tricol.stock.dto.request.RegisterRequest;
import com.tricol.stock.dto.response.AuthResponse;
import com.tricol.stock.dto.response.MessageResponse;
import com.tricol.stock.service.AuditService;
import com.tricol.stock.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final AuditService auditService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            auditService.log("USER_REGISTER", "User", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            auditService.log("USER_LOGIN", "User", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            auditService.log("USER_LOGIN_FAILED", "User", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.of("Invalid credentials"));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.of(e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        try {
            String username = authentication.getName();
            authService.logout(username);
            auditService.log("USER_LOGOUT", "User", username);
            return ResponseEntity.ok(MessageResponse.of("Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }
}
