package com.tricol.stock.service;

import com.tricol.stock.dto.request.RegisterRequest;
import com.tricol.stock.entity.UserApp;
import com.tricol.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing authentication operations with Keycloak
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakAuthService {

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String authServerUrl;

    @Value("${keycloak.realm:tricol-stock}")
    private String realm;

    @Value("${keycloak.resource:tricol-stock-app}")
    private String clientId;

    @Value("${keycloak.credentials.secret:}")
    private String clientSecret;

    private final UserRepository userRepository;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        try {
            keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();

            realmResource = keycloak.realm(realm);
            log.info("Keycloak admin client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak admin client: {}", e.getMessage());
        }
    }

    /**
     * Register a new user in Keycloak and create local database record
     */
    @Transactional
    public Map<String, Object> registerUser(RegisterRequest request) {
        if (realmResource == null) {
            throw new RuntimeException("Keycloak not properly configured");
        }

        // Check if user already exists in database
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        UsersResource usersResource = realmResource.users();

        // Check if user already exists in Keycloak
        List<UserRepresentation> existingUsers = usersResource.search(request.getUsername());
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("Username already exists in Keycloak");
        }

        // Check if email already exists in Keycloak
        List<UserRepresentation> existingEmails = usersResource.searchByEmail(request.getEmail(), true);
        if (!existingEmails.isEmpty()) {
            throw new RuntimeException("Email already exists in Keycloak");
        }

        // Create user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false); // User should verify email

        // Create user in Keycloak
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            String error = response.readEntity(String.class);
            log.error("Failed to create user in Keycloak: {}", error);
            throw new RuntimeException("Failed to create user: " + error);
        }

        // Extract user ID from location header
        String locationHeader = response.getLocation().toString();
        String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        response.close();

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        usersResource.get(keycloakUserId).resetPassword(credential);

        // Assign default role (USER)
        try {
            assignDefaultRole(keycloakUserId);
        } catch (Exception e) {
            log.warn("Failed to assign default role to user {}: {}", request.getUsername(), e.getMessage());
        }

        // Create local database record with Keycloak link
        UserApp localUser = UserApp.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .keycloakUserId(keycloakUserId)
            .password(null) // No local password needed
            .enabled(true)
            .build();

        userRepository.save(localUser);

        log.info("User created in Keycloak and database: {} (Keycloak ID: {})", request.getUsername(), keycloakUserId);

        // Return response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("userId", keycloakUserId);
        responseMap.put("localUserId", localUser.getId());
        responseMap.put("username", request.getUsername());
        responseMap.put("email", request.getEmail());
        responseMap.put("message", "User registered successfully");
        responseMap.put("info", "Please login using Keycloak token endpoint");

        return responseMap;
    }

    /**
     * Assign default USER role to new user
     */
    private void assignDefaultRole(String userId) {
        try {
            var roleRepresentation = realmResource.roles().get("USER").toRepresentation();
            realmResource.users().get(userId).roles().realmLevel()
                .add(Collections.singletonList(roleRepresentation));
            log.info("Assigned USER role to user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to assign USER role: {}", e.getMessage());
            throw new RuntimeException("Failed to assign default role");
        }
    }

    /**
     * Get user by username from Keycloak
     */
    public UserRepresentation getUserByUsername(String username) {
        if (realmResource == null) {
            throw new RuntimeException("Keycloak not properly configured");
        }

        List<UserRepresentation> users = realmResource.users().search(username, true);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Get user by ID from Keycloak
     */
    public UserRepresentation getUserById(String userId) {
        if (realmResource == null) {
            throw new RuntimeException("Keycloak not properly configured");
        }

        return realmResource.users().get(userId).toRepresentation();
    }

    /**
     * Sync local database user with Keycloak user
     * Useful for migrating existing users
     */
    @Transactional
    public void linkUserToKeycloak(String username, String keycloakUserId) {
        UserApp user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setKeycloakUserId(keycloakUserId);
        user.setPassword(null); // Clear local password, now using Keycloak
        userRepository.save(user);

        log.info("Linked user {} to Keycloak ID: {}", username, keycloakUserId);
    }

    /**
     * Check if Keycloak is properly configured and accessible
     */
    public boolean isKeycloakAvailable() {
        try {
            return realmResource != null && realmResource.toRepresentation() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
