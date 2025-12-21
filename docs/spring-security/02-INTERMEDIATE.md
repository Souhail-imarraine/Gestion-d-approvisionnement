# 🟡 Niveau Intermédiaire - Core Spring Security

## 📚 Objectifs d'apprentissage

À la fin de ce niveau, vous serez capable de :
- ✅ Comprendre la Security Filter Chain
- ✅ Implémenter UserDetailsService avec base de données
- ✅ Utiliser BCrypt pour encoder les mots de passe
- ✅ Personnaliser le processus d'authentification
- ✅ Différencier Form Login et HTTP Basic

---

## 🎯 Concepts Clés

### 1. Security Filter Chain

#### Qu'est-ce que c'est ?

Une chaîne de filtres qui intercepte les requêtes HTTP **avant** qu'elles n'atteignent les controllers.

```
Client Request
     ↓
[Filter 1: CSRF Protection]
     ↓
[Filter 2: Authentication]
     ↓
[Filter 3: Authorization]
     ↓
[Filter 4: Exception Handling]
     ↓
Controller
```

#### Filtres principaux

| Filtre | Rôle |
|--------|------|
| **SecurityContextPersistenceFilter** | Charge le contexte de sécurité |
| **UsernamePasswordAuthenticationFilter** | Traite le login |
| **BasicAuthenticationFilter** | Traite HTTP Basic Auth |
| **AuthorizationFilter** | Vérifie les permissions |
| **ExceptionTranslationFilter** | Gère les erreurs de sécurité |

#### Configuration personnalisée

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
}
```

---

### 2. UserDetails & UserDetailsService

#### UserDetails - Interface utilisateur

Représente un utilisateur dans Spring Security.

```java
public interface UserDetails {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities();
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

#### Implémentation personnalisée

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String email;
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

#### UserDetailsService - Chargement utilisateur

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found: " + username)
            );
    }
}
```

---

### 3. PasswordEncoder (BCrypt)

#### Pourquoi encoder les mots de passe ?

❌ **JAMAIS** stocker les mots de passe en clair !

```java
// ❌ MAUVAIS
user.setPassword("password123");

// ✅ BON
user.setPassword(passwordEncoder.encode("password123"));
```

#### BCrypt - Algorithme recommandé

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### Comment ça marche ?

```java
String rawPassword = "myPassword123";
String encodedPassword = passwordEncoder.encode(rawPassword);

// Résultat : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Vérification
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
// true
```

#### Caractéristiques BCrypt

- **Salt automatique** : Chaque hash est unique
- **Coût configurable** : Plus lent = plus sécurisé
- **One-way** : Impossible à décoder

```java
// Configuration avec coût personnalisé
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Coût = 12 (défaut = 10)
}
```

---

### 4. Form Login vs HTTP Basic

#### HTTP Basic Authentication

```java
http.httpBasic(Customizer.withDefaults());
```

**Caractéristiques** :
- Header : `Authorization: Basic base64(username:password)`
- Simple mais moins sécurisé
- Pas de page de login
- Idéal pour APIs

#### Form Login

```java
http.formLogin(form -> form
    .loginPage("/login")
    .loginProcessingUrl("/perform-login")
    .defaultSuccessUrl("/dashboard")
    .failureUrl("/login?error=true")
    .permitAll()
);
```

**Caractéristiques** :
- Page HTML de login
- Session-based
- CSRF protection
- Idéal pour applications web

#### Comparaison

| Aspect | HTTP Basic | Form Login |
|--------|-----------|------------|
| **Type** | Stateless | Stateful |
| **UI** | Popup navigateur | Page HTML |
| **Session** | Non | Oui |
| **CSRF** | Non nécessaire | Nécessaire |
| **Usage** | APIs | Web Apps |

---

## 💻 Pratique - Implémentation complète

### Structure du projet

```
src/main/java/com/example/security/
├── config/
│   └── SecurityConfig.java
├── entity/
│   ├── User.java
│   └── Role.java
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── service/
│   ├── CustomUserDetailsService.java
│   └── UserService.java
└── controller/
    └── UserController.java
```

### 1. Entity User

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
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet());
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
```

### 2. Entity Role

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
    private String name; // ADMIN, USER, MANAGER
}
```

### 3. Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
```

### 4. CustomUserDetailsService

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found: " + username)
            );
    }
}
```

### 5. UserService

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(userRole);
        
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
```

### 6. SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 7. UserController

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword()
        );
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
    
    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        return ResponseEntity.ok(Map.of(
            "username", auth.getName(),
            "authorities", auth.getAuthorities()
        ));
    }
    
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
```

---

## 🎯 Mini-Projet : Système de gestion utilisateurs

### Fonctionnalités
- ✅ Enregistrement utilisateur
- ✅ Authentification avec base de données
- ✅ Gestion des rôles (ADMIN / USER)
- ✅ Endpoints protégés par rôle
- ✅ Mots de passe encodés avec BCrypt

### Tests

```bash
# 1. Enregistrer un utilisateur
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"pass123"}'

# 2. Accéder au profil
curl -u john:pass123 http://localhost:8080/api/user/profile

# 3. Accéder aux utilisateurs (ADMIN uniquement)
curl -u admin:admin123 http://localhost:8080/api/admin/users
```

---

## ✅ Checklist de validation

- [ ] Comprendre la Security Filter Chain
- [ ] Implémenter UserDetailsService avec JPA
- [ ] Encoder les mots de passe avec BCrypt
- [ ] Créer des entités User et Role
- [ ] Configurer l'authentification avec base de données
- [ ] Tester l'enregistrement et l'authentification

---

## ➡️ Prochaine étape

👉 Passez au [Niveau Avancé](./03-ADVANCED.md) pour apprendre JWT et l'authentification stateless
