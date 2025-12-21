# 🔴 Niveau Expert - Enterprise Security

## 📚 Objectifs d'apprentissage

À la fin de ce niveau, vous serez capable de :
- ✅ Implémenter OAuth2 et OpenID Connect
- ✅ Intégrer Keycloak / Auth0
- ✅ Configurer Single Sign-On (SSO)
- ✅ Sécuriser des microservices
- ✅ Appliquer la sécurité au niveau méthode

---

## 🎯 Concepts Clés

### 1. OAuth2 & OpenID Connect

#### OAuth2 - Qu'est-ce que c'est ?

**OAuth2** est un protocole d'**autorisation** qui permet à une application d'accéder aux ressources d'un utilisateur sans connaître son mot de passe.

#### Rôles OAuth2

| Rôle | Description | Exemple |
|------|-------------|---------|
| **Resource Owner** | Propriétaire des données | Utilisateur |
| **Client** | Application qui demande l'accès | Votre app Spring Boot |
| **Authorization Server** | Serveur qui délivre les tokens | Keycloak, Auth0, Google |
| **Resource Server** | Serveur qui héberge les ressources | Votre API |

#### Flow OAuth2 - Authorization Code

```
1. User clicks "Login with Google"
   Client -> Authorization Server: Redirect to login

2. User logs in
   Authorization Server -> User: Login page

3. User approves
   Authorization Server -> Client: Authorization Code

4. Exchange code for token
   Client -> Authorization Server: Code + Client Secret
   Authorization Server -> Client: Access Token

5. Access resource
   Client -> Resource Server: Access Token
   Resource Server -> Client: Protected Data
```

#### OpenID Connect (OIDC)

**OIDC** = OAuth2 + **Authentication**

Ajoute un **ID Token** qui contient les informations de l'utilisateur.

```json
{
  "sub": "248289761001",
  "name": "John Doe",
  "email": "john@example.com",
  "picture": "https://example.com/photo.jpg",
  "iss": "https://accounts.google.com",
  "aud": "your-client-id",
  "exp": 1516239022
}
```

---

### 2. Keycloak Integration

#### Qu'est-ce que Keycloak ?

**Keycloak** est un serveur d'authentification open-source qui fournit :
- Single Sign-On (SSO)
- Identity Brokering (Google, Facebook, etc.)
- User Federation (LDAP, Active Directory)
- Fine-grained Authorization

#### Installation Keycloak (Docker)

```bash
docker run -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

Accès : http://localhost:8180

#### Configuration Keycloak

1. **Créer un Realm** : `my-app-realm`
2. **Créer un Client** : `spring-boot-client`
   - Client Protocol: `openid-connect`
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:8080/*`
3. **Créer des Roles** : `ADMIN`, `USER`
4. **Créer des Users** : Assigner des rôles

---

### 3. Spring Boot + OAuth2 Client

#### Dépendances

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

#### Configuration application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: spring-boot-client
            client-secret: your-client-secret
            scope: openid, profile, email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          keycloak:
            issuer-uri: http://localhost:8180/realms/my-app-realm
            user-name-attribute: preferred_username
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/my-app-realm
```

#### SecurityConfig avec OAuth2

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard", true)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter
        );
        
        return jwtAuthenticationConverter;
    }
}
```

#### Controller avec OAuth2

```java
@RestController
@RequestMapping("/api")
public class UserController {
    
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "name", principal.getAttribute("name"),
            "email", principal.getAttribute("email"),
            "authorities", principal.getAuthorities()
        );
    }
    
    @GetMapping("/user/profile")
    public Map<String, Object> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "username", jwt.getClaimAsString("preferred_username"),
            "email", jwt.getClaimAsString("email"),
            "roles", jwt.getClaimAsStringList("roles")
        );
    }
}
```

---

### 4. Method-Level Security

#### Activer Method Security

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig {
}
```

#### Annotations de sécurité

##### @PreAuthorize

```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        // Seuls les ADMIN peuvent exécuter
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<User> getAllUsers() {
        // ADMIN ou MANAGER
    }
    
    @PreAuthorize("#username == authentication.principal.username")
    public User getUser(String username) {
        // L'utilisateur ne peut accéder qu'à son propre profil
    }
}
```

##### @PostAuthorize

```java
@PostAuthorize("returnObject.owner == authentication.principal.username")
public Document getDocument(Long id) {
    // Vérifie après l'exécution
    return documentRepository.findById(id);
}
```

##### @Secured

```java
@Secured("ROLE_ADMIN")
public void adminOperation() {
    // Seuls les ADMIN
}

@Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
public void managerOperation() {
    // ADMIN ou MANAGER
}
```

##### @RolesAllowed (JSR-250)

```java
@RolesAllowed("ADMIN")
public void deleteAll() {
    // Seuls les ADMIN
}
```

---

### 5. Microservices Security

#### Architecture

```
┌─────────────┐
│   Gateway   │ <- JWT Validation
└──────┬──────┘
       │
   ┌───┴────┬────────┬────────┐
   │        │        │        │
┌──▼──┐  ┌──▼──┐  ┌──▼──┐  ┌──▼──┐
│ MS1 │  │ MS2 │  │ MS3 │  │ MS4 │
└─────┘  └─────┘  └─────┘  └─────┘
```

#### API Gateway avec Spring Cloud Gateway

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

```java
@Configuration
public class GatewaySecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/public/**").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        
        return http.build();
    }
}
```

#### Propagation du token entre microservices

```java
@Configuration
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = 
                SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
```

---

## 💻 Pratique - Projet Enterprise

### Architecture complète

```
Frontend (React/Angular)
         ↓
API Gateway (Spring Cloud Gateway)
         ↓
    ┌────┴────┬────────┬────────┐
    │         │        │        │
User Service  Order   Product  Auth
             Service  Service  Service
    │         │        │        │
    └────┬────┴────────┴────────┘
         ↓
   Keycloak (SSO)
```

### 1. Auth Service

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Déléguer à Keycloak
        // Retourner JWT
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Créer utilisateur dans Keycloak
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Invalider token dans Keycloak
    }
}
```

### 2. User Service

```java
@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
            "username", jwt.getClaimAsString("preferred_username"),
            "email", jwt.getClaimAsString("email")
        ));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }
}
```

### 3. API Gateway Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
          filters:
            - TokenRelay
        
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - TokenRelay
```

---

## 🎯 Projet Final : Backend Enterprise

### Fonctionnalités
- ✅ OAuth2 avec Keycloak
- ✅ Single Sign-On (SSO)
- ✅ JWT Authentication
- ✅ Microservices sécurisés
- ✅ Method-level security
- ✅ Role & Permission management
- ✅ API Gateway

### Tests

```bash
# 1. Login via Keycloak
curl -X POST http://localhost:8180/realms/my-app-realm/protocol/openid-connect/token \
  -d "client_id=spring-boot-client" \
  -d "client_secret=your-secret" \
  -d "grant_type=password" \
  -d "username=john" \
  -d "password=pass123"

# 2. Accéder à l'API avec token
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGc..."

# 3. Accéder à une ressource ADMIN
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## ✅ Checklist de validation

- [ ] Comprendre OAuth2 et OIDC
- [ ] Configurer Keycloak
- [ ] Intégrer OAuth2 dans Spring Boot
- [ ] Implémenter Method-level security
- [ ] Sécuriser des microservices
- [ ] Configurer API Gateway avec JWT

---

## 🎓 Félicitations !

Vous avez terminé la roadmap Spring Security ! Vous êtes maintenant capable de :
- ✅ Sécuriser des applications Spring Boot
- ✅ Implémenter JWT et OAuth2
- ✅ Configurer SSO avec Keycloak
- ✅ Sécuriser des microservices
- ✅ Appliquer les meilleures pratiques de sécurité

---

## 📚 Ressources supplémentaires

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [OAuth2 RFC](https://oauth.net/2/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [JWT.io](https://jwt.io/)

---

👉 Consultez les [Bonnes Pratiques](./06-BEST_PRACTICES.md) pour la production
