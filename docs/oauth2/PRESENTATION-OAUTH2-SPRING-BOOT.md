# 10 Points Clés pour Présentation OAuth2 Spring Boot

## 1. Configuration de Base Spring Security OAuth2

**Dépendances Maven nécessaires:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Configuration application.yml:**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://accounts.google.com
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
```

---

## 2. SecurityConfig - Protection des Endpoints

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

---

## 3. JWT Token Validation

```java
@Configuration
public class JwtConfig {
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://your-auth-server.com");
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }
}
```

---

## 4. OAuth2 Client Configuration (Login avec Google/GitHub)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: profile, email
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user, user:email
```

```java
@Configuration
public class OAuth2LoginConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
            );
        
        return http.build();
    }
}
```

---

## 5. Récupération des Informations Utilisateur

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "username", jwt.getClaimAsString("sub"),
            "email", jwt.getClaimAsString("email"),
            "roles", jwt.getClaimAsStringList("roles")
        );
    }
    
    @GetMapping("/oauth2-info")
    public String getOAuth2User(@AuthenticationPrincipal OAuth2User principal) {
        return "Welcome " + principal.getAttribute("name");
    }
}
```

---

## 6. Authorization Server (Serveur d'Autorisation)

```java
@Configuration
public class AuthorizationServerConfig {
    
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("client-app")
            .clientSecret("{noop}secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/login/oauth2/code/client-app")
            .scope("read")
            .scope("write")
            .build();
        
        return new InMemoryRegisteredClientRepository(client);
    }
}
```

---

## 7. Scopes et Permissions Personnalisés

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public List<Product> getProducts() {
        return productService.findAll();
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_write')")
    public Product createProduct(@RequestBody Product product) {
        return productService.save(product);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
```

---

## 8. Refresh Token Implementation

```java
@Service
public class TokenService {
    
    @Autowired
    private JwtEncoder jwtEncoder;
    
    public String generateAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(15, ChronoUnit.MINUTES))
            .subject(username)
            .claim("roles", roles)
            .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(7, ChronoUnit.DAYS))
            .subject(username)
            .claim("type", "refresh")
            .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
```

---

## 9. CORS Configuration pour OAuth2

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## 10. Testing OAuth2 Endpoints

```java
@SpringBootTest
@AutoConfigureMockMvc
public class OAuth2SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testProtectedEndpointWithJwt() throws Exception {
        String token = generateMockJwt();
        
        mockMvc.perform(get("/api/products")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());
    }
    
    private String generateMockJwt() {
        return Jwts.builder()
            .setSubject("testuser")
            .claim("roles", Arrays.asList("ROLE_USER"))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS256, "secret-key")
            .compact();
    }
}
```

---

## Résumé des Points Clés

| Point | Concept | Utilisation |
|-------|---------|-------------|
| 1 | Configuration de base | Dépendances et properties |
| 2 | SecurityConfig | Protection des endpoints |
| 3 | JWT Validation | Validation et décodage des tokens |
| 4 | OAuth2 Client | Login social (Google, GitHub) |
| 5 | User Info | Récupération des données utilisateur |
| 6 | Authorization Server | Création de serveur d'autorisation |
| 7 | Scopes | Gestion des permissions granulaires |
| 8 | Refresh Token | Renouvellement des tokens |
| 9 | CORS | Configuration pour applications frontend |
| 10 | Testing | Tests unitaires et d'intégration |

---

## Flow Complet d'Authentification

```
1. Client → Authorization Server: Demande d'autorisation
2. Authorization Server → User: Page de login
3. User → Authorization Server: Credentials
4. Authorization Server → Client: Authorization Code
5. Client → Authorization Server: Exchange code for token
6. Authorization Server → Client: Access Token + Refresh Token
7. Client → Resource Server: Request with Access Token
8. Resource Server → Client: Protected Resource
```
