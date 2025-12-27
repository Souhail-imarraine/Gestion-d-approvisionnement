# Flow Détaillé OAuth2 Spring Security - من الصفر حتى النهاية

## Table des Matières
1. [Installation et Configuration Initiale](#etape-1-installation)
2. [Création du Projet](#etape-2-creation-projet)
3. [Configuration de la Base de Données](#etape-3-database)
4. [Création des Entités](#etape-4-entities)
5. [Configuration Spring Security](#etape-5-security-config)
6. [Implémentation JWT](#etape-6-jwt)
7. [Création des Services](#etape-7-services)
8. [Création des Controllers](#etape-8-controllers)
9. [Flow Complet d'Authentification](#etape-9-flow-auth)
10. [Testing et Validation](#etape-10-testing)

---

## ÉTAPE 1: Installation et Configuration Initiale

### 1.1 Prérequis
```bash
# Vérifier Java (minimum JDK 17)
java -version

# Vérifier Maven
mvn -version

# IDE: IntelliJ IDEA ou VS Code
```

### 1.2 Créer le Projet Spring Boot
```bash
# Via Spring Initializr (https://start.spring.io)
# Ou via commande Maven
mvn archetype:generate -DgroupId=com.tricol.stock \
  -DartifactId=oauth2-demo \
  -DarchetypeArtifactId=maven-archetype-quickstart
```

### 1.3 Dépendances dans pom.xml
```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    
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
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- JPA & MySQL -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

**شنو واقع هنا؟**
- Spring Security: للحماية ديال الـ endpoints
- OAuth2 Resource Server: باش نقدرو نvalidيو الـ JWT tokens
- JJWT: مكتبة باش نصنعو و نvalidيو الـ tokens
- JPA + MySQL: باش نخزنو المستخدمين و الـ roles

---

## ÉTAPE 2: Configuration application.properties

```properties
# Server Configuration
server.port=8081
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/oauth2_db
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation123456789
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

**شنو واقع هنا؟**
- Server port 8081 و context-path /api
- Connection ديال MySQL على localhost
- JWT secret key (خاص يكون طويل و معقد)
- Expiration: 15 دقيقة للـ access token، 7 أيام للـ refresh token

---

## ÉTAPE 3: Création des Entités

### 3.1 Entity User
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return enabled; }
}
```

**شنو واقع هنا؟**
- User implements UserDetails: باش Spring Security يقدر يستعملها
- ManyToMany مع Role: واحد المستخدم يقدر يكون عندو بزاف ديال الـ roles
- getAuthorities(): ترجع الـ permissions ديال المستخدم

### 3.2 Entity Role
```java
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}
```

### 3.3 Entity RefreshToken
```java
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    private boolean revoked = false;
}
```

**شنو واقع هنا؟**
- RefreshToken: باش نخزنو الـ refresh tokens فـ database
- expiryDate: باش نعرفو واش الـ token مازال صالح
- revoked: باش نقدرو نلغيو الـ token (logout)

---

## ÉTAPE 4: Configuration Spring Security

### 4.1 SecurityConfig
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
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

**شنو واقع هنا؟**
- csrf.disable(): حيت غادي نستعملو JWT (stateless)
- /auth/** permitAll: الـ endpoints ديال login و register مفتوحين
- STATELESS: ما كاينش session، كلشي بـ JWT
- jwtAuthFilter: فيلتر باش نvalidيو الـ token قبل ما ندخلو للـ controller

---

## ÉTAPE 5: Implémentation JWT Service

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
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
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
```

**شنو واقع هنا؟**
- generateToken(): كيصنع JWT token فيه username و roles
- extractUsername(): كيستخرج username من الـ token
- isTokenValid(): كيتحقق واش الـ token صالح و ماشي expired
- HS256: الـ algorithm ديال الـ signature

---

## ÉTAPE 6: JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
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
        
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**شنو واقع هنا؟**
1. كيجيب الـ Authorization header
2. كيتحقق واش فيه "Bearer " فالبداية
3. كيستخرج الـ token و الـ username
4. كيvalidي الـ token
5. كيحط الـ authentication فـ SecurityContext
6. كيكمل للـ controller

---

## ÉTAPE 7: Authentication Service

```java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        
        userRepository.save(user);
        
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        
        return new AuthResponse(accessToken, refreshToken);
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        
        return new AuthResponse(accessToken, refreshToken);
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
                String accessToken = jwtService.generateToken(user);
                return new AuthResponse(accessToken, refreshToken);
            })
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
}
```

**شنو واقع هنا؟**
- register(): كيسجل مستخدم جديد و كيرجع tokens
- login(): كيتحقق من credentials و كيرجع tokens
- refreshToken(): كيجدد الـ access token بـ refresh token

---

## ÉTAPE 8: Authentication Controller

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
```

---

## ÉTAPE 9: Flow Complet d'Authentification

### 9.1 Registration Flow
```
1. User → POST /auth/register
   Body: { "username": "ahmed", "email": "ahmed@test.com", "password": "123456" }

2. AuthController → AuthService.register()

3. AuthService:
   - كيشفر الـ password بـ BCrypt
   - كيحفظ User فـ database
   - كيصنع access token (15 min)
   - كيصنع refresh token (7 days)

4. Response:
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
   }
```

### 9.2 Login Flow
```
1. User → POST /auth/login
   Body: { "username": "ahmed", "password": "123456" }

2. AuthController → AuthService.login()

3. AuthService:
   - AuthenticationManager كيتحقق من credentials
   - كيجيب User من database
   - كيصنع access token جديد
   - كيصنع refresh token جديد

4. Response: { "accessToken": "...", "refreshToken": "..." }
```

### 9.3 Protected Endpoint Flow
```
1. User → GET /api/products
   Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter:
   - كيستخرج الـ token من header
   - كيvalidي الـ token
   - كيستخرج username و roles
   - كيحط authentication فـ SecurityContext

3. Spring Security:
   - كيتحقق من @PreAuthorize
   - كيسمح بالوصول إذا كان عندو الـ permission

4. Controller → Service → Repository → Database

5. Response: List<Product>
```

### 9.4 Refresh Token Flow
```
1. Access Token expired (بعد 15 دقيقة)

2. User → POST /auth/refresh
   Body: { "refreshToken": "550e8400-e29b-41d4-a716-446655440000" }

3. AuthService:
   - كيتحقق واش refresh token صالح
   - كيجيب User المرتبط بيه
   - كيصنع access token جديد

4. Response: { "accessToken": "...", "refreshToken": "..." }
```

---

## ÉTAPE 10: Testing avec Postman

### 10.1 Register
```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "username": "ahmed",
  "email": "ahmed@test.com",
  "password": "123456"
}
```

### 10.2 Login
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "ahmed",
  "password": "123456"
}
```

### 10.3 Access Protected Endpoint
```http
GET http://localhost:8081/api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 10.4 Refresh Token
```http
POST http://localhost:8081/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Résumé du Flow Complet

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. POST /auth/login
       │    {username, password}
       ▼
┌─────────────────────┐
│  AuthController     │
└──────┬──────────────┘
       │ 2. authService.login()
       ▼
┌─────────────────────┐
│   AuthService       │
│  - Authenticate     │
│  - Generate JWT     │
│  - Create Refresh   │
└──────┬──────────────┘
       │ 3. Return tokens
       ▼
┌─────────────┐
│   Client    │ Store tokens
└──────┬──────┘
       │ 4. GET /api/products
       │    Header: Bearer <token>
       ▼
┌─────────────────────┐
│ JwtAuthFilter       │
│  - Extract token    │
│  - Validate token   │
│  - Set auth context │
└──────┬──────────────┘
       │ 5. Token valid
       ▼
┌─────────────────────┐
│ SecurityConfig      │
│  - Check @PreAuth   │
│  - Verify roles     │
└──────┬──────────────┘
       │ 6. Authorized
       ▼
┌─────────────────────┐
│  ProductController  │
└──────┬──────────────┘
       │ 7. Return data
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

**الخلاصة:**
1. User كيسجل أو كيدخل → كيرجع access token + refresh token
2. Client كيخزن الـ tokens
3. كل request كيدير Authorization header بـ access token
4. JwtAuthFilter كيvalidي الـ token قبل ما يوصل للـ controller
5. Spring Security كيتحقق من الـ permissions
6. إذا access token expired، كيستعمل refresh token باش يجدد
