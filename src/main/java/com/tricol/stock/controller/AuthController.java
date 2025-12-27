package com.tricol.stock.controller;

import com.tricol.stock.dto.request.RegisterRequest;
import com.tricol.stock.dto.response.MessageResponse;
import com.tricol.stock.service.AuditService;
import com.tricol.stock.service.KeycloakAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for OAuth2/Keycloak Integration
 *
 * Note: Login and token refresh are now handled directly by Keycloak.
 * Clients should call Keycloak's token endpoint:
 * POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final KeycloakAuthService keycloakAuthService;
    private final AuditService auditService;
    
    /**
     * Register a new user in Keycloak
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = keycloakAuthService.registerUser(request);
            auditService.log("USER_REGISTER", "User", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }
    
    /**
     * Get current authenticated user information from JWT token
     * GET /api/v1/auth/me
     *
     * Requires: Authorization: Bearer <access_token>
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", jwt.getSubject());
            userInfo.put("username", jwt.getClaimAsString("preferred_username"));
            userInfo.put("email", jwt.getClaimAsString("email"));
            userInfo.put("emailVerified", jwt.getClaimAsBoolean("email_verified"));
            userInfo.put("name", jwt.getClaimAsString("name"));
            userInfo.put("firstName", jwt.getClaimAsString("given_name"));
            userInfo.put("lastName", jwt.getClaimAsString("family_name"));

            // Extract roles from realm_access claim
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                userInfo.put("roles", realmAccess.get("roles"));
            }

            // Extract scopes
            String scope = jwt.getClaimAsString("scope");
            if (scope != null) {
                userInfo.put("scopes", scope.split(" "));
            }

            userInfo.put("tokenIssuedAt", jwt.getIssuedAt());
            userInfo.put("tokenExpiresAt", jwt.getExpiresAt());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.of("Invalid token"));
        }
    }
    
    /**
     * Logout endpoint - logs the action for audit purposes
     * POST /api/v1/auth/logout
     *
     * Note: Client should also revoke the token with Keycloak:
     * POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Jwt jwt) {
        try {
            String username = jwt.getClaimAsString("preferred_username");
            auditService.log("USER_LOGOUT", "User", username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            response.put("info", "Token will expire naturally. For immediate revocation, call Keycloak logout endpoint.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    /**
     * Health check endpoint - public access
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("authProvider", "Keycloak OAuth2");
        health.put("timestamp", java.time.Instant.now());
        return ResponseEntity.ok(health);
    }

    /**
     * Get Keycloak configuration info for frontend
     * GET /api/v1/auth/config
     */
    @GetMapping("/config")
    public ResponseEntity<?> getKeycloakConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("realm", "tricol-stock");
        config.put("authServerUrl", "http://localhost:8180");
        config.put("tokenEndpoint", "http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token");
        config.put("logoutEndpoint", "http://localhost:8180/realms/tricol-stock/protocol/openid-connect/logout");
        config.put("userInfoEndpoint", "http://localhost:8180/realms/tricol-stock/protocol/openid-connect/userinfo");
        config.put("clientId", "tricol-stock-app");
        return ResponseEntity.ok(config);
    }
}
