package com.tricol.stock.service;

import com.tricol.stock.dto.request.LoginRequest;
import com.tricol.stock.dto.request.RegisterRequest;
import com.tricol.stock.dto.response.AuthResponse;
import com.tricol.stock.entity.UserApp;
import com.tricol.stock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserApp testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = UserApp.builder()
                .id(1L)
                .username("testuser")
                .email("test@tricol.com")
                .password("encodedPassword")
                .enabled(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@tricol.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        UserApp savedUser = UserApp.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@tricol.com")
                .password("encodedPassword")
                .enabled(false)
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserApp.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@tricol.com", response.getEmail());
        verify(userRepository, times(1)).save(any(UserApp.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testLogin_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void testRefreshToken_Success() {
        String refreshTokenString = "validRefreshToken";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("newAccessToken");

        AuthResponse response = authService.refreshToken(refreshTokenString);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
    }

    @Test
    void testRefreshToken_ExpiredToken() {
        String refreshTokenString = "expiredToken";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(testUser)
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(refreshTokenString);
        });
        assertEquals("Refresh token expired", exception.getMessage());
    }

    @Test
    void testLogout_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        authService.logout("testuser");

        verify(refreshTokenRepository, times(1)).deleteByUserId(testUser.getId());
    }
}
