# 🔐 Spring Security OAuth2

## 📖 Dans ce Chapitre

1. Architecture Spring Security OAuth2
2. Authorization Server
3. Resource Server
4. Client Configuration
5. Projet Complet

---

## 🏗️ Architecture Spring Security OAuth2

### Composants Principaux

```
┌─────────────────────────────────────────┐
│     Spring Security OAuth2              │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  Authorization Server            │  │
│  │  - Génère tokens                 │  │
│  │  - Gère consentement             │  │
│  │  - Endpoints: /authorize, /token │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  Resource Server                 │  │
│  │  - Valide tokens                 │  │
│  │  - Protège APIs                  │  │
│  │  - Vérifie scopes                │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  OAuth2 Client                   │  │
│  │  - Obtient tokens                │  │
│  │  - Appelle APIs                  │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## 1️⃣ Authorization Server

### Dependencies (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-oauth2-authorization-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### Configuration

```java
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) 
            throws Exception {
        OAuth2AuthorizationServerConfiguration
            .applyDefaultSecurity(http);
        
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults());
        
        http.exceptionHandling(exceptions -> exceptions
            .defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        );
        
        return http.build();
    }
    
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) 
            throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());
        
        return http.build();
    }
    
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
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build())
            .build();
        
        return new InMemoryRegisteredClientRepository(client);
    }
    
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
        
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
    
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
    
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
    
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
```

### application.yml

```yaml
server:
  port: 9000

spring:
  application:
    name: auth-server
  datasource:
    url: jdbc:mysql://localhost:3306/oauth2_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 2️⃣ Resource Server

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        
        return http.build();
    }
}
```

### application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: resource-server
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
```

### Protected Controller

```java
@RestController
@RequestMapping("/api")
public class PhotoController {
    
    @GetMapping("/photos")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public List<Photo> getPhotos() {
        return photoService.findAll();
    }
    
    @PostMapping("/photos")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    public Photo createPhoto(@RequestBody Photo photo) {
        return photoService.save(photo);
    }
    
    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "sub", jwt.getSubject(),
            "scopes", jwt.getClaimAsStringList("scope"),
            "exp", jwt.getExpiresAt()
        );
    }
}
```

---

## 3️⃣ OAuth2 Client

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          my-client:
            client-id: client-app
            client-secret: secret
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - read
              - write
        provider:
          my-client:
            authorization-uri: http://localhost:9000/oauth2/authorize
            token-uri: http://localhost:9000/oauth2/token
            user-info-uri: http://localhost:9000/userinfo
            jwk-set-uri: http://localhost:9000/oauth2/jwks
```

### Configuration

```java
@Configuration
@EnableWebSecurity
public class ClientSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2Login(Customizer.withDefaults())
            .oauth2Client(Customizer.withDefaults());
        
        return http.build();
    }
}
```

### Using OAuth2 Client

```java
@Controller
public class ClientController {
    
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    
    @GetMapping("/photos")
    public String getPhotos(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService
            .loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
            );
        
        String accessToken = client.getAccessToken().getTokenValue();
        
        // Call Resource Server
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(
            "http://localhost:8081/api/photos",
            HttpMethod.GET,
            entity,
            List.class
        );
        
        model.addAttribute("photos", response.getBody());
        return "photos";
    }
}
```

---

## 📚 Fichiers Créés (4/6)

✅ 01-INTRODUCTION.md  
✅ 02-CONCEPTS-FONDAMENTAUX.md  
✅ 03-OAUTH2-FLOWS.md  
✅ 04-SPRING-SECURITY-OAUTH2.md  
⏳ 05-PROJET-PRATIQUE.md  
⏳ 06-PRODUCTION-BEST-PRACTICES.md

**Guide OAuth2 presque terminé!** 🎉
