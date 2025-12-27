package com.tricol.stock.repository;

import com.tricol.stock.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserApp, Long> {
    
    Optional<UserApp> findByUsername(String username);
    
    Optional<UserApp> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    /**
     * Find user by their Keycloak User ID
     * @param keycloakUserId The Keycloak user identifier
     * @return Optional containing the user if found
     */
    Optional<UserApp> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Check if a Keycloak user ID already exists
     * @param keycloakUserId The Keycloak user identifier
     * @return true if exists, false otherwise
     */
    boolean existsByKeycloakUserId(String keycloakUserId);
}
