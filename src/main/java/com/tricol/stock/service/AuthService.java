package com.tricol.stock.service;

import com.tricol.stock.dto.request.LoginRequest;
import com.tricol.stock.dto.request.RegisterRequest;
import com.tricol.stock.dto.response.AuthResponse;
import com.tricol.stock.entity.RefreshToken;
import com.tricol.stock.entity.UserApp;
import com.tricol.stock.repository.RefreshTokenRepository;
import com.tricol.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        UserApp user = UserApp.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .enabled(false)
            .build();
        
        userRepository.save(user);
        
        return AuthResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserApp user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        saveRefreshToken(user, refreshToken);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900000L)
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getUsername());
        String newAccessToken = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900000L)
            .username(token.getUser().getUsername())
            .email(token.getUser().getEmail())
            .build();
    }
    
    @Transactional
    public void logout(String username) {
        UserApp user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUserId(user.getId());
    }
    
    private void saveRefreshToken(UserApp user, String token) {
        refreshTokenRepository.deleteByUserId(user.getId());
        
        RefreshToken refreshToken = RefreshToken.builder()
            .token(token)
            .user(user)
            .expiryDate(Instant.now().plusMillis(604800000L))
            .build();
        
        refreshTokenRepository.save(refreshToken);
    }
}
