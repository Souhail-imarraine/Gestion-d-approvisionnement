# 🟠 Niveau Avancé - API & Token Security

## 📚 Objectifs d'apprentissage

À la fin de ce niveau, vous serez capable de :
- ✅ Comprendre JWT (JSON Web Token)
- ✅ Implémenter l'authentification stateless
- ✅ Gérer Access Token et Refresh Token
- ✅ Sécuriser des APIs REST pour frontend (React/Angular)
- ✅ Créer un filtre JWT personnalisé

---

## 🎯 Concepts Clés

### 1. JWT (JSON Web Token)

#### Qu'est-ce que JWT ?

Un **token signé** qui contient des informations (claims) sur l'utilisateur.

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

#### Structure JWT

```
HEADER.PAYLOAD.SIGNATURE
```

**1. Header** (Algorithme)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**2. Payload** (Données)
```json
{
  "sub": "john@example.com",
  "name": "John Doe",
  "roles": ["USER", "ADMIN"],
  "iat": 1516239022,
  "exp": 1516242622
}
```

**3. Signature** (Vérification)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

#### Pourquoi JWT ?

| Avantage | Description |
|----------|-------------|
| **Stateless** | Pas de session serveur |
| **Scalable** | Fonctionne avec microservices |
| **Cross-domain** | Utilisable sur plusieurs domaines |
| **Mobile-friendly** | Idéal pour apps mobiles |

---

### 2. Access Token & Refresh Token

#### Access Token
- **Durée** : Courte (15-30 minutes)
- **Usage** : Accéder aux ressources protégées
- **Stockage** : Mémoire (pas localStorage)

#### Refresh Token
- **Durée** : Longue (7-30 jours)
- **Usage** : Obtenir un nouveau Access Token
- **Stockage** : HttpOnly Cookie (sécurisé)

#### Flow complet

```
1. Login
   Client -> Server: username + password
   Server -> Client: Access Token + Refresh Token

2. Accès ressource
   Client -> Server: Access Token
   Server -> Client: Données

3. Token expiré
   Client -> Server: Refresh Token
   Server -> Client: Nouveau Access Token

4. Refresh Token expiré
   Client -> Server: Refresh Token
   Server -> Client: 401 Unauthorized (re-login)
```

---

### 3. Stateless Security

#### Session-based (Stateful)

```
Client                    Server
  |                         |
  |---- Login ------------->| Store session in memory
  |<--- JSESSIONID ---------|
  |                         |
  |---- Request + Cookie -->| Check session
  |<--- Response -----------|
```

❌ Problèmes :
- Mémoire serveur utilisée
- Difficile à scaler
- Pas adapté aux microservices

#### Token-based (Stateless)

```
Client                    Server
  |                         |
  |---- Login ------------->| Generate JWT
  |<--- JWT Token ----------|
  |                         |
  |---- Request + JWT ----->| Verify JWT signature
  |<--- Response -----------|
```

✅ Avantages :
- Pas de mémoire serveur
- Facile à scaler
- Adapté aux microservices

---

## 💻 Pratique - Implémentation JWT

### Dépendances Maven

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

### 1. JwtUtil - Génération et validation

```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration; // 15 minutes
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration; // 7 jours
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Générer Access Token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Générer Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Extraire username du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extraire expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // Extraire claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    // Vérifier si token expiré
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // Valider token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

### 2. JwtAuthenticationFilter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtUtil.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 3. SecurityConfig avec JWT

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 4. AuthController - Login & Refresh

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.getRefreshToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (!jwtUtil.isTokenExpired(request.getRefreshToken())) {
                String newAccessToken = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Refresh token expired"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid refresh token"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Côté client : supprimer les tokens
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
```

### 5. DTOs

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
    private String refreshToken;
}
```

### 6. application.properties

```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

---

## 🎯 Mini-Projet : API REST avec JWT

### Fonctionnalités
- ✅ Login avec JWT
- ✅ Refresh token
- ✅ Logout
- ✅ Endpoints protégés
- ✅ Gestion des rôles

### Tests avec cURL

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123"}'

# Réponse:
# {
#   "accessToken": "eyJhbGc...",
#   "refreshToken": "eyJhbGc..."
# }

# 2. Accéder à une ressource protégée
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGc..."

# 3. Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGc..."}'

# 4. Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## ✅ Checklist de validation

- [ ] Comprendre la structure JWT
- [ ] Implémenter génération et validation JWT
- [ ] Créer un filtre JWT personnalisé
- [ ] Gérer Access Token et Refresh Token
- [ ] Configurer sécurité stateless
- [ ] Tester login, refresh, logout

---

## ➡️ Prochaine étape

👉 Passez au [Niveau Expert](./04-EXPERT.md) pour apprendre OAuth2 et OpenID Connect
