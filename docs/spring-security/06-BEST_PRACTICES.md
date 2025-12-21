# ✅ Bonnes Pratiques de Sécurité

## Table des Matières

1. [Password Security](#1-password-security)
2. [HTTPS & Transport Security](#2-https--transport-security)
3. [Error Handling](#3-error-handling)
4. [Token Management](#4-token-management)
5. [Principle of Least Privilege](#5-principle-of-least-privilege)
6. [Input Validation](#6-input-validation)
7. [Logging & Monitoring](#7-logging--monitoring)
8. [Session Management](#8-session-management)
9. [API Security](#9-api-security)
10. [Production Checklist](#10-production-checklist)

---

## 1. Password Security

### ✅ À FAIRE

#### Toujours hasher les mots de passe

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Coût élevé pour plus de sécurité
}
```

#### Politique de mots de passe forte

```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
)
private String password;
```

#### Vérifier les mots de passe compromis

```java
@Service
public class PasswordValidationService {
    
    public boolean isPasswordCompromised(String password) {
        // Utiliser Have I Been Pwned API
        String sha1 = DigestUtils.sha1Hex(password);
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);
        
        // Appeler l'API et vérifier
        return checkPwnedPasswordsAPI(prefix, suffix);
    }
}
```

### ❌ À ÉVITER

```java
// ❌ JAMAIS faire ça !
user.setPassword("password123"); // Mot de passe en clair

// ❌ Algorithmes faibles
new MessageDigestPasswordEncoder("MD5");
new MessageDigestPasswordEncoder("SHA-1");

// ❌ Pas de salt
String hash = DigestUtils.md5Hex(password);
```

---

## 2. HTTPS & Transport Security

### ✅ Toujours utiliser HTTPS en production

#### Configuration Spring Boot

```properties
# application.properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

#### Forcer HTTPS

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .requiresChannel(channel -> channel
            .anyRequest().requiresSecure()
        );
    
    return http.build();
}
```

### Strict Transport Security (HSTS)

```java
http.headers(headers -> headers
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000) // 1 an
    )
);
```

### Security Headers

```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'")
    )
    .xssProtection(xss -> xss.block(true))
    .frameOptions(frame -> frame.deny())
    .contentTypeOptions(Customizer.withDefaults())
);
```

---

## 3. Error Handling

### ✅ Ne jamais exposer d'informations sensibles

#### Mauvais exemple ❌

```java
catch (Exception e) {
    return ResponseEntity.status(500)
        .body("Error: " + e.getMessage()); // ❌ Expose des détails
}
```

#### Bon exemple ✅

```java
@RestControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        // ✅ Message générique
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid credentials"));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        // ✅ Pas de détails
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Access denied"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        // ✅ Log l'erreur, mais ne l'expose pas
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "An error occurred"));
    }
}
```

### Messages d'erreur sécurisés

```java
// ❌ Mauvais
"User 'john@example.com' not found"
"Password incorrect for user 'john@example.com'"

// ✅ Bon
"Invalid credentials"
"Authentication failed"
```

---

## 4. Token Management

### Access Token

#### Durée de vie courte

```properties
jwt.expiration=900000  # 15 minutes
```

#### Stockage sécurisé

```javascript
// ❌ JAMAIS dans localStorage
localStorage.setItem('token', token);

// ✅ En mémoire ou sessionStorage
const token = sessionStorage.getItem('token');

// ✅✅ HttpOnly Cookie (meilleur)
Set-Cookie: token=...; HttpOnly; Secure; SameSite=Strict
```

### Refresh Token

#### Durée de vie longue mais limitée

```properties
jwt.refresh-expiration=604800000  # 7 jours
```

#### Rotation des refresh tokens

```java
@PostMapping("/refresh")
public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
    String oldRefreshToken = request.getRefreshToken();
    
    // Valider l'ancien token
    if (!jwtUtil.validateRefreshToken(oldRefreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // Générer nouveaux tokens
    String newAccessToken = jwtUtil.generateAccessToken(user);
    String newRefreshToken = jwtUtil.generateRefreshToken(user);
    
    // ✅ Invalider l'ancien refresh token
    refreshTokenRepository.deleteByToken(oldRefreshToken);
    
    // ✅ Sauvegarder le nouveau
    refreshTokenRepository.save(new RefreshToken(newRefreshToken, user));
    
    return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
}
```

### Révocation de tokens

```java
@Entity
public class TokenBlacklist {
    @Id
    private String token;
    private Date expirationDate;
}

@Service
public class TokenBlacklistService {
    
    public void blacklistToken(String token) {
        TokenBlacklist blacklisted = new TokenBlacklist();
        blacklisted.setToken(token);
        blacklisted.setExpirationDate(jwtUtil.extractExpiration(token));
        repository.save(blacklisted);
    }
    
    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }
}
```

---

## 5. Principle of Least Privilege

### Accorder uniquement les permissions nécessaires

#### ❌ Mauvais

```java
@PreAuthorize("hasRole('ADMIN')")
public List<User> getUsers() {
    return userRepository.findAll();
}
```

#### ✅ Bon

```java
@PreAuthorize("hasAuthority('READ_USERS')")
public List<User> getUsers() {
    return userRepository.findAll();
}
```

### Hiérarchie des rôles

```java
@Configuration
public class RoleHierarchyConfig {
    
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
            "ROLE_ADMIN > ROLE_MANAGER\n" +
            "ROLE_MANAGER > ROLE_USER"
        );
        return hierarchy;
    }
}
```

### Permissions granulaires

```java
public enum Permission {
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_DELETE("user:delete"),
    ORDER_READ("order:read"),
    ORDER_WRITE("order:write");
    
    private final String permission;
}
```

---

## 6. Input Validation

### Toujours valider les entrées

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO dto) {
    // @Valid déclenche la validation
}

@Data
public class UserDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    private String password;
}
```

### Sanitization

```java
@Service
public class InputSanitizer {
    
    public String sanitize(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
    
    public String sanitizeSQL(String input) {
        // Utiliser des PreparedStatements au lieu de concaténation
        return input.replaceAll("[';\"\\-\\-]", "");
    }
}
```

### Protection contre injection SQL

```java
// ❌ JAMAIS faire ça
@Query(value = "SELECT * FROM users WHERE username = '" + username + "'", nativeQuery = true)
User findByUsername(String username);

// ✅ Utiliser des paramètres
@Query("SELECT u FROM User u WHERE u.username = :username")
User findByUsername(@Param("username") String username);
```

---

## 7. Logging & Monitoring

### Logger les événements de sécurité

```java
@Component
@Slf4j
public class SecurityEventListener {
    
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Successful login: {}", username);
    }
    
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        log.warn("Failed login attempt: {}", username);
    }
    
    @EventListener
    public void onAuthorizationFailure(AuthorizationDeniedEvent event) {
        String username = event.getAuthentication().get().getName();
        log.warn("Access denied for user: {}", username);
    }
}
```

### Audit Trail

```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    
    private String username;
    private String action;
    private String resource;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
}

@Service
public class AuditService {
    
    public void logAction(String action, String resource) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        AuditLog log = new AuditLog();
        log.setUsername(auth.getName());
        log.setAction(action);
        log.setResource(resource);
        log.setTimestamp(LocalDateTime.now());
        
        auditRepository.save(log);
    }
}
```

### ❌ Ne jamais logger d'informations sensibles

```java
// ❌ JAMAIS
log.info("User logged in with password: {}", password);
log.debug("JWT token: {}", token);
log.info("Credit card: {}", creditCard);

// ✅ Bon
log.info("User logged in: {}", username);
log.debug("Token generated for user: {}", username);
log.info("Payment processed for user: {}", username);
```

---

## 8. Session Management

### Configuration sécurisée

```java
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1) // Une seule session par utilisateur
    .maxSessionsPreventsLogin(true) // Empêcher nouvelle connexion
    .expiredUrl("/login?expired")
);
```

### Session Fixation Protection

```java
http.sessionManagement(session -> session
    .sessionFixation().newSession() // Créer nouvelle session après login
);
```

### Cookie sécurisé

```properties
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
```

---

## 9. API Security

### Rate Limiting

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String clientId = request.getRemoteAddr();
        int count = requestCounts.getOrDefault(clientId, 0);
        
        if (count >= MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        
        requestCounts.put(clientId, count + 1);
        filterChain.doFilter(request, response);
    }
}
```

### API Versioning

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    // Version 1
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    // Version 2 avec améliorations de sécurité
}
```

### Content-Type Validation

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> create(@RequestBody UserDTO dto) {
    // Accepte uniquement JSON
}
```

---

## 10. Production Checklist

### ✅ Avant le déploiement

- [ ] HTTPS activé
- [ ] Mots de passe hashés avec BCrypt (coût >= 10)
- [ ] CSRF protection activée (si session-based)
- [ ] CORS configuré correctement
- [ ] Security headers configurés
- [ ] Logs de sécurité activés
- [ ] Rate limiting implémenté
- [ ] Input validation sur tous les endpoints
- [ ] Error handling sécurisé
- [ ] Tokens avec expiration courte
- [ ] Refresh token rotation
- [ ] Session timeout configuré
- [ ] Secrets externalisés (pas dans le code)
- [ ] Dépendances à jour
- [ ] Tests de sécurité effectués

### Configuration Production

```properties
# application-prod.properties

# HTTPS
server.ssl.enabled=true
server.port=8443

# Security Headers
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# JWT
jwt.secret=${JWT_SECRET}  # Variable d'environnement
jwt.expiration=900000  # 15 minutes

# Logging
logging.level.org.springframework.security=INFO
logging.level.com.yourapp.security=DEBUG

# Actuator (monitoring)
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized
```

### Variables d'environnement

```bash
# .env (JAMAIS commiter ce fichier)
JWT_SECRET=your-super-secret-key-change-this-in-production
DB_PASSWORD=your-database-password
OAUTH_CLIENT_SECRET=your-oauth-secret
```

### Docker Secrets

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    image: your-app:latest
    secrets:
      - jwt_secret
      - db_password
    environment:
      JWT_SECRET_FILE: /run/secrets/jwt_secret
      DB_PASSWORD_FILE: /run/secrets/db_password

secrets:
  jwt_secret:
    external: true
  db_password:
    external: true
```

---

## 📚 Ressources supplémentaires

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/features/exploits/index.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

---

## 🎯 Résumé des règles d'or

1. ✅ **Toujours** hasher les mots de passe
2. ✅ **Toujours** utiliser HTTPS en production
3. ✅ **Jamais** exposer d'informations sensibles dans les erreurs
4. ✅ **Toujours** valider les entrées utilisateur
5. ✅ **Toujours** logger les événements de sécurité
6. ✅ **Jamais** stocker de secrets dans le code
7. ✅ **Toujours** appliquer le principe du moindre privilège
8. ✅ **Toujours** utiliser des tokens avec expiration
9. ✅ **Toujours** configurer les security headers
10. ✅ **Toujours** tester la sécurité avant le déploiement

---

👉 Consultez le [Projet Final](./07-FINAL_PROJECT.md)
