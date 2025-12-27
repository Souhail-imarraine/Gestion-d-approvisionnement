# Keycloak Integration Guide for Tricol Stock Management

## Table of Contents
1. [Introduction](#introduction)
2. [Why Keycloak?](#why-keycloak)
3. [Prerequisites](#prerequisites)
4. [Architecture Overview](#architecture-overview)
5. [Step-by-Step Integration](#step-by-step-integration)
6. [Configuration Details](#configuration-details)
7. [Code Migration Strategy](#code-migration-strategy)
8. [Testing](#testing)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices](#best-practices)

---

## Introduction

This guide explains how to integrate Keycloak as an Identity and Access Management (IAM) solution for the Tricol Stock Management application. Keycloak will replace the current custom JWT authentication system with a production-ready, enterprise-grade authentication and authorization solution.

**Current State:**
- Custom JWT implementation
- Manual user management
- Custom authentication endpoints
- Database-stored users and roles

**Target State:**
- Keycloak-managed authentication
- OAuth 2.0 / OpenID Connect
- Centralized user management
- Single Sign-On (SSO) capable
- Enhanced security features

---

## Why Keycloak?

### Benefits Over Custom JWT Implementation

| Feature | Custom JWT | Keycloak |
|---------|-----------|----------|
| **User Management** | Manual DB operations | Web UI + Admin API |
| **Token Management** | Custom code | Built-in with refresh rotation |
| **Password Policies** | Custom validation | Configurable policies |
| **MFA/2FA** | Need to implement | Built-in support |
| **SSO** | Not available | Native support |
| **Social Login** | Need to implement | Google, Facebook, etc. |
| **Token Introspection** | Custom implementation | Standard endpoint |
| **Session Management** | Manual | Full session control |
| **Audit Logs** | Custom implementation | Built-in |
| **Standards Compliance** | Custom | OAuth 2.0, OIDC, SAML |

### Key Advantages

1. **Production-Ready Security**: Industry-standard security practices
2. **Reduced Development Time**: No need to maintain authentication code
3. **Scalability**: Built for enterprise applications
4. **Flexibility**: Support for multiple authentication flows
5. **Community Support**: Large community and extensive documentation

---

## Prerequisites

### Software Requirements
- Java 17+
- Spring Boot 3.5.7 (current version)
- Docker & Docker Compose
- Maven 3.6+

### Knowledge Requirements
- Basic understanding of OAuth 2.0 and OpenID Connect
- Spring Security fundamentals
- REST API concepts

---

## Architecture Overview

### Current Architecture
```
Client → Spring Boot App → JWT Filter → Custom Auth → Database
                          ↓
                    Custom User Service
```

### Target Architecture with Keycloak
```
Client → Spring Boot App → Keycloak Adapter → Keycloak Server
                          ↓
                    Resource Server (Your API)
                          ↓
                    Business Logic
```

### Authentication Flow

```
1. User Login Request
   ↓
2. Redirect to Keycloak Login Page
   ↓
3. User Enters Credentials
   ↓
4. Keycloak Validates Credentials
   ↓
5. Keycloak Issues Access Token + Refresh Token
   ↓
6. Client Stores Tokens
   ↓
7. Client Sends Access Token with API Requests
   ↓
8. Spring Boot Validates Token with Keycloak
   ↓
9. Grant Access to Protected Resources
```

---

## Step-by-Step Integration

### Phase 1: Setup Keycloak Server

#### 1.1 Update Docker Compose

Add Keycloak to your `docker-compose.yml`:

```yaml
services:
  # ... existing services (mysql, phpmyadmin, app)

  keycloak:
    image: quay.io/keycloak/keycloak:23.0.0
    container_name: tricol-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: mysql
      KC_DB_URL: jdbc:mysql://mysql:3306/keycloak_db?createDatabaseIfNotExist=true
      KC_DB_USERNAME: root
      KC_DB_PASSWORD: root
      KC_HOSTNAME_STRICT: false
      KC_HTTP_ENABLED: true
      KC_HOSTNAME_STRICT_HTTPS: false
    ports:
      - "8180:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - tricol-network
    command:
      - start-dev
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5
```

#### 1.2 Start Keycloak

```bash
docker-compose up -d keycloak
```

Wait for Keycloak to start (check logs):
```bash
docker-compose logs -f keycloak
```

Access Keycloak Admin Console:
- URL: `http://localhost:8180`
- Username: `admin`
- Password: `admin`

---

### Phase 2: Configure Keycloak

#### 2.1 Create Realm

1. Login to Keycloak Admin Console
2. Click dropdown "Keycloak" (top left) → **Add realm**
3. Name: `tricol-stock`
4. Click **Create**

#### 2.2 Create Client

1. Go to **Clients** → Click **Create**
2. Configure:
   - **Client ID**: `tricol-stock-app`
   - **Client Protocol**: `openid-connect`
   - **Root URL**: `http://localhost:8081/tricol-stock`
3. Click **Save**

4. Configure Client Settings:
   - **Access Type**: `confidential`
   - **Standard Flow Enabled**: `ON`
   - **Direct Access Grants Enabled**: `ON`
   - **Service Accounts Enabled**: `ON`
   - **Authorization Enabled**: `ON`
   - **Valid Redirect URIs**: 
     - `http://localhost:8081/tricol-stock/*`
     - `http://localhost:3000/*` (if you have a frontend)
   - **Web Origins**: `*` (for development, restrict in production)
5. Click **Save**

6. Go to **Credentials** tab:
   - Copy the **Secret** (you'll need this)

#### 2.3 Create Roles

1. Go to **Roles** → Click **Add Role**
2. Create these roles:
   - `ROLE_ADMIN`
   - `ROLE_MANAGER`
   - `ROLE_USER`
   - `ROLE_GUEST`

For each role:
- Name: (as above)
- Description: (appropriate description)
- Click **Save**

#### 2.4 Create Client Scopes (for fine-grained permissions)

1. Go to **Client Scopes** → Click **Create**
2. Create scopes matching your permissions:
   - `produit:read`, `produit:write`, `produit:delete`
   - `commande:read`, `commande:write`, `commande:delete`
   - `fournisseur:read`, `fournisseur:write`, `fournisseur:delete`
   - `stock:read`, `stock:write`
   - `user:manage`
   - `audit:read`

For each scope:
- **Name**: (as above)
- **Protocol**: `openid-connect`
- **Display On Consent Screen**: `ON`
- Click **Save**

#### 2.5 Map Scopes to Client

1. Go to **Clients** → `tricol-stock-app` → **Client Scopes** tab
2. Add the created scopes to **Assigned Default Client Scopes**

#### 2.6 Create Test Users

1. Go to **Users** → Click **Add user**
2. Create admin user:
   - **Username**: `admin`
   - **Email**: `admin@tricol.com`
   - **First Name**: `Admin`
   - **Last Name**: `User`
   - **Email Verified**: `ON`
   - **Enabled**: `ON`
3. Click **Save**

4. Set Password:
   - Go to **Credentials** tab
   - **Password**: `admin123`
   - **Temporary**: `OFF`
   - Click **Set Password**

5. Assign Roles:
   - Go to **Role Mappings** tab
   - Select `ROLE_ADMIN` from **Available Roles**
   - Click **Add selected**

Repeat for other test users (manager, user, guest).

---

### Phase 3: Update Spring Boot Application

#### 3.1 Update Dependencies (pom.xml)

Add Keycloak Spring Boot Adapter:

```xml
<!-- Remove or comment out existing JWT dependencies -->
<!--
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
-->

<!-- Add Keycloak Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- For JWT decoding -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

#### 3.2 Update Application Properties

Replace JWT configuration with Keycloak configuration:

```properties
# Remove old JWT config
# jwt.secret=...
# jwt.expiration=...
# jwt.refresh-expiration=...

# Keycloak Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/tricol-stock/protocol/openid-connect/certs

# OAuth2 Client Configuration (if using authorization code flow)
spring.security.oauth2.client.registration.keycloak.client-id=tricol-stock-app
spring.security.oauth2.client.registration.keycloak.client-secret=YOUR_CLIENT_SECRET_HERE
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email,roles
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8180/realms/tricol-stock
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username

# Keycloak Admin Client (for user management)
keycloak.auth-server-url=http://localhost:8180
keycloak.realm=tricol-stock
keycloak.resource=tricol-stock-app
keycloak.credentials.secret=YOUR_CLIENT_SECRET_HERE
keycloak.use-resource-role-mappings=true
keycloak.bearer-only=true
```

#### 3.3 Create New Security Configuration

Create `KeycloakSecurityConfig.java`:

```java
package com.tricol.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Protected endpoints
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Collection<String> realmRoles = realmAccess != null 
                ? (Collection<String>) realmAccess.get("roles") 
                : List.of();

            // Extract resource roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            Collection<String> resourceRoles = List.of();
            if (resourceAccess != null && resourceAccess.containsKey("tricol-stock-app")) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("tricol-stock-app");
                resourceRoles = (Collection<String>) clientAccess.get("roles");
            }

            // Combine and convert to GrantedAuthority
            return Stream.concat(
                realmRoles.stream(),
                resourceRoles.stream()
            )
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toSet());
        });
        
        return converter;
    }
}
```

#### 3.4 Create Keycloak Service for User Management

Create `KeycloakAdminService.java`:

```java
package com.tricol.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        keycloak = KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .grantType("client_credentials")
            .build();
        
        realmResource = keycloak.realm(realm);
    }

    public String createUser(String username, String email, String firstName, 
                            String lastName, String password) {
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Create user
        var response = usersResource.create(user);
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        usersResource.get(userId).resetPassword(credential);

        log.info("User created in Keycloak: {}", username);
        return userId;
    }

    public void assignRole(String userId, String roleName) {
        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(userId).roles().realmLevel()
            .add(Collections.singletonList(role));
        
        log.info("Role {} assigned to user {}", roleName, userId);
    }

    public void deleteUser(String userId) {
        realmResource.users().delete(userId);
        log.info("User deleted from Keycloak: {}", userId);
    }

    public List<UserRepresentation> getAllUsers() {
        return realmResource.users().list();
    }

    public UserRepresentation getUserByUsername(String username) {
        List<UserRepresentation> users = realmResource.users().search(username);
        return users.isEmpty() ? null : users.get(0);
    }
}
```

#### 3.5 Update Controllers

Update your controllers to use Keycloak authentication:

```java
package com.tricol.stock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExampleController {

    // Get current user info from JWT
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "username", jwt.getClaimAsString("preferred_username"),
            "email", jwt.getClaimAsString("email"),
            "name", jwt.getClaimAsString("name"),
            "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }

    // Role-based access control
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "Admin content";
    }

    // Permission-based access control
    @PostMapping("/produits")
    @PreAuthorize("hasAuthority('produit:write')")
    public String createProduct() {
        return "Product created";
    }
}
```

---

### Phase 4: Migration Strategy

#### Option 1: Big Bang Migration (Recommended for Development)

1. **Backup current database**
2. **Stop the application**
3. **Start Keycloak**
4. **Configure Keycloak (as per Phase 2)**
5. **Update application code (as per Phase 3)**
6. **Migrate existing users to Keycloak**
7. **Test thoroughly**
8. **Start application with new configuration**

#### Option 2: Gradual Migration (Recommended for Production)

1. **Run Keycloak alongside existing auth**
2. **Create feature flag for Keycloak auth**
3. **Migrate users in batches**
4. **Test with subset of users**
5. **Gradually increase Keycloak usage**
6. **Monitor for issues**
7. **Complete migration**
8. **Remove old auth code**

#### User Migration Script

Create `UserMigrationService.java`:

```java
package com.tricol.stock.service;

import com.tricol.stock.entity.UserApp;
import com.tricol.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("migrate-users")
@RequiredArgsConstructor
@Slf4j
public class UserMigrationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting user migration to Keycloak...");

        var users = userRepository.findAll();
        int migrated = 0;
        int failed = 0;

        for (UserApp user : users) {
            try {
                // Check if user already exists in Keycloak
                var existingUser = keycloakAdminService.getUserByUsername(user.getUsername());
                if (existingUser != null) {
                    log.warn("User {} already exists in Keycloak, skipping", user.getUsername());
                    continue;
                }

                // Create user in Keycloak
                String userId = keycloakAdminService.createUser(
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    "ChangeMe123!" // Temporary password, user should reset
                );

                // Assign roles
                user.getRoles().forEach(role -> {
                    keycloakAdminService.assignRole(userId, role.getName());
                });

                migrated++;
                log.info("Migrated user: {}", user.getUsername());

            } catch (Exception e) {
                failed++;
                log.error("Failed to migrate user: {}", user.getUsername(), e);
            }
        }

        log.info("Migration completed. Migrated: {}, Failed: {}", migrated, failed);
    }
}
```

Run migration:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=migrate-users
```

---

## Configuration Details

### Environment-Specific Configuration

#### Development (`application-dev.properties`)
```properties
keycloak.auth-server-url=http://localhost:8180
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
```

#### Production (`application-prod.properties`)
```properties
keycloak.auth-server-url=https://keycloak.yourdomain.com
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://keycloak.yourdomain.com/realms/tricol-stock
keycloak.ssl-required=external
```

### Docker Configuration for Production

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:23.0.0
  environment:
    KC_DB: mysql
    KC_DB_URL: jdbc:mysql://mysql:3306/keycloak_db
    KC_DB_USERNAME: ${DB_USER}
    KC_DB_PASSWORD: ${DB_PASSWORD}
    KC_HOSTNAME: keycloak.yourdomain.com
    KC_PROXY: edge
    KEYCLOAK_ADMIN: ${ADMIN_USER}
    KEYCLOAK_ADMIN_PASSWORD: ${ADMIN_PASSWORD}
  command:
    - start
    - --optimized
    - --https-certificate-file=/etc/certs/cert.pem
    - --https-certificate-key-file=/etc/certs/key.pem
```

---

## Testing

### 1. Test Keycloak Setup

```bash
# Get access token
curl -X POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=tricol-stock-app" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password"
```

Response:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cC...",
  "token_type": "Bearer"
}
```

### 2. Test API with Token

```bash
# Use the access token
curl -X GET http://localhost:8081/tricol-stock/api/v1/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Test Role-Based Access

```bash
# Admin endpoint (should work with admin token)
curl -X GET http://localhost:8081/tricol-stock/api/v1/admin/users \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN"

# Admin endpoint (should fail with user token)
curl -X GET http://localhost:8081/tricol-stock/api/v1/admin/users \
  -H "Authorization: Bearer USER_ACCESS_TOKEN"
```

### 4. Integration Tests

Create `KeycloakIntegrationTest.java`:

```java
@SpringBootTest
@Testcontainers
class KeycloakIntegrationTest {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer()
        .withRealmImportFile("test-realm.json");

    @Test
    void testAuthentication() {
        // Test authentication flow
    }

    @Test
    void testAuthorization() {
        // Test role-based access
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. **Token Validation Fails**

**Problem**: `Invalid token issuer`

**Solution**:
- Check `issuer-uri` matches Keycloak realm
- Ensure Keycloak is accessible from Spring Boot
- Verify network connectivity

```properties
# Make sure these match
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
```

#### 2. **Roles Not Extracted**

**Problem**: User authenticated but has no roles

**Solution**:
- Check JWT token claims (use jwt.io to decode)
- Verify `JwtAuthenticationConverter` extracts roles correctly
- Ensure roles are assigned in Keycloak

#### 3. **CORS Issues**

**Problem**: Browser blocks requests

**Solution**:
Add CORS configuration in Keycloak and Spring Boot:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

#### 4. **Keycloak Container Won't Start**

**Problem**: Database connection issues

**Solution**:
- Ensure MySQL is running first
- Check database credentials
- Verify network configuration

```bash
# Check logs
docker-compose logs keycloak

# Restart with clean state
docker-compose down -v
docker-compose up -d
```

#### 5. **Client Secret Not Working**

**Problem**: Authentication fails with client credentials

**Solution**:
- Regenerate secret in Keycloak Admin Console
- Update `application.properties`
- Restart application

---

## Best Practices

### Security

1. **Use HTTPS in Production**
   - Never use HTTP for Keycloak in production
   - Configure SSL certificates properly

2. **Strong Client Secrets**
   - Generate cryptographically strong secrets
   - Rotate secrets regularly
   - Store in environment variables, not in code

3. **Token Expiration**
   - Access tokens: 5-15 minutes
   - Refresh tokens: 30 days max
   - Enable refresh token rotation

4. **Password Policies**
   - Minimum length: 12 characters
   - Require uppercase, lowercase, numbers, special chars
   - Enable password history

5. **Enable MFA**
   - Require for admin accounts
   - Offer for all users

### Performance

1. **Token Caching**
   - Cache JWK keys
   - Use connection pooling

2. **Database Optimization**
   - Index Keycloak tables
   - Regular cleanup of sessions

3. **Clustering**
   - Use multiple Keycloak instances
   - Enable distributed cache (Infinispan)

### Monitoring

1. **Health Checks**
   ```properties
   management.endpoint.health.show-details=always
   management.health.keycloak.enabled=true
   ```

2. **Logging**
   ```properties
   logging.level.org.keycloak=INFO
   logging.level.org.springframework.security=DEBUG
   ```

3. **Metrics**
   - Monitor token issuance rate
   - Track failed login attempts
   - Alert on unusual patterns

### Development Workflow

1. **Local Development**
   - Use Docker Compose for Keycloak
   - Import/export realm configurations
   - Version control realm JSON files

2. **Testing**
   - Use Testcontainers for integration tests
   - Mock tokens for unit tests
   - Separate test realm

3. **CI/CD**
   - Automate realm configuration
   - Test migrations
   - Validate security rules

---

## Summary

### What You Gain

✅ **Enterprise-grade security** out of the box  
✅ **Reduced maintenance** - no custom auth code  
✅ **SSO capabilities** for future expansion  
✅ **Better user experience** with forgot password, email verification, etc.  
✅ **Compliance** with OAuth 2.0 and OIDC standards  
✅ **Scalability** for growing user base  
✅ **Flexibility** for multiple authentication methods  

### What You Lose

❌ Full control over authentication flow  
❌ Simplicity of custom JWT (but gain robustness)  
❌ One less moving part (but Keycloak is stable)  

### Next Steps

1. ✅ Read this guide thoroughly
2. ✅ Setup Keycloak in Docker
3. ✅ Configure realm and clients
4. ✅ Update Spring Boot dependencies
5. ✅ Implement new security configuration
6. ✅ Test with Postman/curl
7. ✅ Migrate existing users
8. ✅ Update frontend (if applicable)
9. ✅ Deploy to staging
10. ✅ Monitor and optimize

---

## Additional Resources

### Documentation
- [Keycloak Official Docs](https://www.keycloak.org/documentation)
- [Spring Security OAuth 2.0](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [OAuth 2.0 RFC](https://oauth.net/2/)
- [OpenID Connect](https://openid.net/connect/)

### Tutorials
- [Keycloak with Spring Boot](https://www.baeldung.com/spring-boot-keycloak)
- [Securing REST APIs](https://www.keycloak.org/docs/latest/securing_apps/)

### Community
- [Keycloak GitHub](https://github.com/keycloak/keycloak)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/keycloak)
- [Keycloak Discourse](https://keycloak.discourse.group/)

---

## Support

If you encounter issues during integration:

1. Check Keycloak logs: `docker-compose logs keycloak`
2. Check application logs for Spring Security errors
3. Verify JWT token claims at [jwt.io](https://jwt.io)
4. Review this guide's troubleshooting section
5. Consult official Keycloak documentation

---

**Last Updated**: December 26, 2025  
**Version**: 1.0  
**Author**: Tricol Stock Management Team

