# Migration from Custom JWT to Keycloak - Side by Side Comparison

## Overview

This document shows exactly what changes when migrating from your current custom JWT implementation to Keycloak.

---

## 1. Dependencies Changes

### BEFORE (Custom JWT)
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### AFTER (Keycloak)
```xml
<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Keycloak Admin Client (optional, for user management) -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>23.0.0</version>
</dependency>
```

---

## 2. Configuration Changes

### BEFORE (application.properties)
```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

### AFTER (application.properties)
```properties
# Keycloak Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/tricol-stock/protocol/openid-connect/certs

keycloak.auth-server-url=http://localhost:8180
keycloak.realm=tricol-stock
keycloak.resource=tricol-stock-app
keycloak.credentials.secret=YOUR_CLIENT_SECRET_FROM_KEYCLOAK
```

---

## 3. Security Configuration

### BEFORE (SecurityConfig.java)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
```

### AFTER (KeycloakSecurityConfig.java)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
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
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) return Collections.emptySet();
            
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
        });
        return converter;
    }
}
```

**Key Differences:**
- ❌ No custom JWT filter needed
- ❌ No custom UserDetailsService needed
- ❌ No AuthenticationProvider needed
- ✅ OAuth2 Resource Server handles everything
- ✅ JWT validation automatic via JWKS endpoint

---

## 4. Authentication Flow

### BEFORE (Custom JWT)
```
1. POST /api/v1/auth/login
   Body: {"username": "user", "password": "pass"}
   
2. AuthService validates against database
   
3. Generate JWT with custom JwtService
   
4. Return token to client
   
5. Client sends: Authorization: Bearer <custom-jwt>
   
6. JwtAuthenticationFilter validates token
   
7. Load user from database
   
8. Grant access
```

### AFTER (Keycloak)
```
1. POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token
   Body: {
     "client_id": "tricol-stock-app",
     "client_secret": "secret",
     "username": "user",
     "password": "pass",
     "grant_type": "password"
   }
   
2. Keycloak validates credentials
   
3. Keycloak generates JWT (signed with its private key)
   
4. Return access_token + refresh_token
   
5. Client sends: Authorization: Bearer <keycloak-jwt>
   
6. Spring Security validates with Keycloak's public key (from JWKS)
   
7. Extract roles from JWT claims
   
8. Grant access
```

---

## 5. Files That Can Be DELETED

Once Keycloak is working, you can safely delete:

```
❌ JwtAuthenticationFilter.java
❌ JwtService.java
❌ CustomUserDetailsService.java (if only used for auth)
❌ AuthService.java (login/register methods)
❌ RefreshToken.java entity (Keycloak manages this)
❌ RefreshTokenRepository.java
```

---

## 6. User Management

### BEFORE (Database-based)
```java
// Create user
UserApp user = UserApp.builder()
    .username("john")
    .password(passwordEncoder.encode("password"))
    .email("john@example.com")
    .build();
userRepository.save(user);

// Assign role
RoleApp role = roleRepository.findByName("ADMIN");
user.getRoles().add(role);
userRepository.save(user);
```

### AFTER (Keycloak-based)
```java
// Create user
keycloakAdminService.createUser(
    "john", 
    "john@example.com", 
    "John", 
    "Doe", 
    "password"
);

// Assign role
keycloakAdminService.assignRole(userId, "ADMIN");
```

**OR** via Keycloak Admin UI (http://localhost:8180)

---

## 7. Controller Changes

### BEFORE
```java
@PostMapping("/login")
public AuthResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
}

@GetMapping("/me")
public UserDTO getCurrentUser(Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    return userService.getUserById(userDetails.getId());
}

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<UserDTO> getAllUsers() {
    return userService.getAllUsers();
}
```

### AFTER
```java
// Login handled by Keycloak - no endpoint needed
// Client calls Keycloak directly

@GetMapping("/me")
public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
    return Map.of(
        "username", jwt.getClaimAsString("preferred_username"),
        "email", jwt.getClaimAsString("email"),
        "name", jwt.getClaimAsString("name"),
        "roles", jwt.getClaimAsMap("realm_access").get("roles")
    );
}

@PreAuthorize("hasRole('ADMIN')")  // Same as before!
@GetMapping("/admin/users")
public List<UserDTO> getAllUsers() {
    return userService.getAllUsers();
}
```

**Key Differences:**
- ❌ No /login endpoint in your app
- ✅ Use `@AuthenticationPrincipal Jwt` instead of `Authentication`
- ✅ `@PreAuthorize` works the same way

---

## 8. Testing API Calls

### BEFORE (Custom JWT)
```bash
# 1. Login
curl -X POST http://localhost:8081/tricol-stock/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response: {"accessToken": "eyJ...", "refreshToken": "..."}

# 2. Use token
curl -X GET http://localhost:8081/tricol-stock/api/v1/produits \
  -H "Authorization: Bearer eyJ..."
```

### AFTER (Keycloak)
```bash
# 1. Get token from Keycloak
curl -X POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=tricol-stock-app" \
  -d "client_secret=YOUR_SECRET" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password"

# Response: {"access_token": "eyJ...", "refresh_token": "...", "expires_in": 300}

# 2. Use token (same as before!)
curl -X GET http://localhost:8081/tricol-stock/api/v1/produits \
  -H "Authorization: Bearer eyJ..."
```

---

## 9. JWT Token Comparison

### BEFORE (Custom JWT Claims)
```json
{
  "sub": "admin",
  "iat": 1703593200,
  "exp": 1703594100,
  "roles": ["ADMIN", "USER"]
}
```

### AFTER (Keycloak JWT Claims)
```json
{
  "exp": 1703594100,
  "iat": 1703593200,
  "jti": "a1b2c3d4-...",
  "iss": "http://localhost:8180/realms/tricol-stock",
  "sub": "f47ac10b-...",
  "typ": "Bearer",
  "azp": "tricol-stock-app",
  "preferred_username": "admin",
  "email": "admin@tricol.com",
  "email_verified": true,
  "name": "Admin User",
  "given_name": "Admin",
  "family_name": "User",
  "realm_access": {
    "roles": ["ADMIN", "USER"]
  },
  "resource_access": {
    "tricol-stock-app": {
      "roles": ["app-admin"]
    }
  },
  "scope": "openid profile email"
}
```

**More Information in Keycloak Tokens:**
- ✅ User ID (sub)
- ✅ Email verification status
- ✅ Full name, given name, family name
- ✅ Client-specific roles
- ✅ Scopes
- ✅ Token ID for revocation

---

## 10. Database Schema Impact

### Tables You Can KEEP (still useful)
```
✅ users (for app-specific user data)
✅ roles (for reference, but roles managed in Keycloak)
✅ permissions (for fine-grained permissions)
✅ audit_logs (still track user actions)
```

### Tables You Can REMOVE (Keycloak manages these)
```
❌ refresh_tokens (Keycloak manages refresh tokens)
```

### Recommended: Keep User Table but Change Usage
```sql
-- BEFORE: Used for authentication
-- AFTER: Use for app-specific data only

-- Link to Keycloak user via external ID
ALTER TABLE users ADD COLUMN keycloak_user_id VARCHAR(255);
ALTER TABLE users ADD INDEX idx_keycloak_user_id (keycloak_user_id);

-- Username becomes reference, not auth credential
-- Password field no longer needed for auth
```

---

## 11. Error Handling

### BEFORE (Custom Errors)
```java
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(401).body("Invalid username or password");
}
```

### AFTER (Keycloak Errors)
```java
@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<?> handleAuthError(AuthenticationException ex) {
    return ResponseEntity.status(401).body("Invalid or expired token");
}

// Keycloak returns standard OAuth2 errors:
// - invalid_grant: Wrong credentials
// - invalid_token: Expired or invalid token
// - insufficient_scope: Missing permissions
```

---

## 12. Development Workflow

### BEFORE
```bash
1. Start MySQL
2. Run migrations
3. Start Spring Boot app
4. Test with Postman
```

### AFTER
```bash
1. Start MySQL
2. Start Keycloak (docker-compose up -d keycloak)
3. Configure realm (one-time setup)
4. Run migrations
5. Start Spring Boot app
6. Test with Postman (different endpoints)
```

---

## 13. Advantages Summary

| Feature | Custom JWT | Keycloak |
|---------|-----------|----------|
| **Code to Maintain** | High | Low |
| **Setup Complexity** | Low | Medium |
| **Security Features** | Basic | Enterprise |
| **User Management UI** | Need to build | Built-in |
| **Password Reset** | Need to implement | Built-in |
| **Email Verification** | Need to implement | Built-in |
| **MFA/2FA** | Need to implement | Built-in |
| **Social Login** | Need to implement | Built-in |
| **Token Rotation** | Manual | Automatic |
| **Session Management** | Manual | Full control |
| **Audit Logs** | Custom | Built-in |
| **Scalability** | Manual | Built for scale |

---

## 14. Migration Checklist

### Phase 1: Preparation
- [ ] Read full Keycloak integration guide
- [ ] Backup current database
- [ ] Document current authentication flow
- [ ] Test current implementation thoroughly

### Phase 2: Setup Keycloak
- [ ] Add Keycloak to docker-compose.yml
- [ ] Start Keycloak container
- [ ] Access admin console (http://localhost:8180)
- [ ] Create realm: tricol-stock
- [ ] Create client: tricol-stock-app
- [ ] Copy client secret
- [ ] Create roles (ADMIN, MANAGER, USER, GUEST)
- [ ] Create test users

### Phase 3: Update Spring Boot
- [ ] Update pom.xml dependencies
- [ ] Remove old JWT dependencies
- [ ] Add OAuth2 resource server dependency
- [ ] Update application.properties
- [ ] Create KeycloakSecurityConfig
- [ ] Remove/rename old SecurityConfig
- [ ] Test compilation

### Phase 4: Code Migration
- [ ] Update controllers to use Jwt instead of UserDetails
- [ ] Create KeycloakAdminService (optional)
- [ ] Update user-related DTOs
- [ ] Modify audit logging to use JWT claims
- [ ] Remove unused auth endpoints

### Phase 5: Testing
- [ ] Test token generation via Keycloak
- [ ] Test API calls with Keycloak tokens
- [ ] Test role-based access control
- [ ] Test token expiration and refresh
- [ ] Test error scenarios

### Phase 6: Cleanup
- [ ] Delete old JWT classes
- [ ] Remove unused database tables
- [ ] Update documentation
- [ ] Clean up imports

### Phase 7: Production Preparation
- [ ] Setup HTTPS for Keycloak
- [ ] Configure production database
- [ ] Enable proper password policies
- [ ] Setup email server for notifications
- [ ] Configure session timeout
- [ ] Enable audit logging

---

## Quick Decision Guide

**Choose Custom JWT if:**
- Very simple authentication needs
- Small application, few users
- Don't need advanced features
- Want minimal dependencies

**Choose Keycloak if:**
- Growing application
- Need enterprise features (MFA, SSO, etc.)
- Want to focus on business logic, not auth
- Multiple applications needing same auth
- Compliance requirements
- Need centralized user management

**For Tricol Stock Management:** Keycloak is recommended because:
1. ✅ You already have complex role/permission system
2. ✅ Audit requirements suggest enterprise use
3. ✅ Room for growth (multiple clients, SSO)
4. ✅ Reduced security maintenance burden

---

## Still Have Questions?

See the full guides:
- **Detailed Integration**: [KEYCLOAK_INTEGRATION_GUIDE.md](KEYCLOAK_INTEGRATION_GUIDE.md)
- **Quick Start**: [keycloak/QUICK_START.md](keycloak/QUICK_START.md)
- **Postman Collection**: [keycloak/postman-collection.json](keycloak/postman-collection.json)

