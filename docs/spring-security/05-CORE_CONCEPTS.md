# 📖 Concepts Fondamentaux - Référence Complète

## Table des Matières

1. [Authentication vs Authorization](#1-authentication-vs-authorization)
2. [Security Filter Chain](#2-security-filter-chain)
3. [UserDetails & UserDetailsService](#3-userdetails--userdetailsservice)
4. [Password Encoding](#4-password-encoding)
5. [Role vs Permission](#5-role-vs-permission)
6. [JWT](#6-jwt)
7. [OAuth2 & OIDC](#7-oauth2--oidc)
8. [CSRF Protection](#8-csrf-protection)
9. [CORS Configuration](#9-cors-configuration)

---

## 1. Authentication vs Authorization

### Authentication (Authentification)

**Définition** : Processus de vérification de l'identité d'un utilisateur.

**Question** : "Qui êtes-vous ?"

**Méthodes** :
- Username + Password
- Token (JWT, OAuth2)
- Biométrie (empreinte, visage)
- Certificats SSL/TLS
- Multi-Factor Authentication (MFA)

**Exemple** :
```java
Authentication auth = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

### Authorization (Autorisation)

**Définition** : Processus de vérification des permissions d'un utilisateur authentifié.

**Question** : "Que pouvez-vous faire ?"

**Méthodes** :
- Role-Based Access Control (RBAC)
- Permission-Based Access Control
- Attribute-Based Access Control (ABAC)

**Exemple** :
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
    // Seuls les ADMIN peuvent exécuter
}
```

### Comparaison

| Aspect | Authentication | Authorization |
|--------|----------------|---------------|
| **Objectif** | Vérifier l'identité | Vérifier les permissions |
| **Moment** | Première étape | Après authentication |
| **Résultat** | Utilisateur identifié | Accès accordé/refusé |
| **Exemple** | Login avec email/password | Accès à /admin/users |

---

## 2. Security Filter Chain

### Qu'est-ce que c'est ?

Une **chaîne de filtres** qui intercepte chaque requête HTTP avant qu'elle n'atteigne les controllers.

### Architecture

```
HTTP Request
     ↓
┌────────────────────────────────┐
│  Security Filter Chain         │
├────────────────────────────────┤
│ 1. SecurityContextPersistence  │ <- Charge le contexte de sécurité
│ 2. LogoutFilter                │ <- Gère la déconnexion
│ 3. UsernamePasswordAuth        │ <- Traite le login
│ 4. BasicAuthenticationFilter   │ <- HTTP Basic Auth
│ 5. RequestCacheAwareFilter     │ <- Cache des requêtes
│ 6. SecurityContextHolder       │ <- Stocke l'authentification
│ 7. AnonymousAuthenticationF    │ <- Utilisateur anonyme
│ 8. SessionManagementFilter     │ <- Gestion des sessions
│ 9. ExceptionTranslationFilter  │ <- Gère les exceptions
│ 10. AuthorizationFilter        │ <- Vérifie les autorisations
└────────────────────────────────┘
     ↓
Controller
```

### Filtres principaux

#### SecurityContextPersistenceFilter
- Charge le SecurityContext depuis la session
- Sauvegarde le SecurityContext après la requête

#### UsernamePasswordAuthenticationFilter
- Intercepte POST /login
- Extrait username et password
- Authentifie l'utilisateur

#### BasicAuthenticationFilter
- Traite l'en-tête Authorization: Basic
- Décode Base64(username:password)

#### AuthorizationFilter
- Vérifie si l'utilisateur a les permissions nécessaires
- Utilise les règles définies dans SecurityConfig

### Configuration personnalisée

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(loggingFilter, BasicAuthenticationFilter.class);
    
    return http.build();
}
```

---

## 3. UserDetails & UserDetailsService

### UserDetails

**Interface** qui représente un utilisateur dans Spring Security.

```java
public interface UserDetails extends Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();
    String getPassword();
    String getUsername();
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

### Implémentation personnalisée

```java
@Entity
public class User implements UserDetails {
    
    @Id
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());
    }
    
    // Autres méthodes...
}
```

### UserDetailsService

**Interface** pour charger les utilisateurs depuis une source de données.

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException;
}
```

### Implémentation

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found: " + username)
            );
    }
}
```

---

## 4. Password Encoding

### Pourquoi encoder ?

❌ **JAMAIS** stocker les mots de passe en clair !

**Risques** :
- Vol de base de données
- Accès non autorisé
- Violation de données personnelles

### PasswordEncoder

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

### Algorithmes disponibles

| Algorithme | Sécurité | Performance | Recommandé |
|------------|----------|-------------|------------|
| **BCrypt** | ⭐⭐⭐⭐⭐ | Lent | ✅ Oui |
| **Argon2** | ⭐⭐⭐⭐⭐ | Lent | ✅ Oui |
| **PBKDF2** | ⭐⭐⭐⭐ | Moyen | ⚠️ OK |
| **SCrypt** | ⭐⭐⭐⭐⭐ | Très lent | ✅ Oui |
| **SHA-256** | ⭐⭐ | Rapide | ❌ Non |
| **MD5** | ⭐ | Rapide | ❌ Non |

### BCrypt - Recommandé

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Caractéristiques** :
- Salt automatique (unique pour chaque hash)
- Coût configurable (10 par défaut)
- One-way (impossible à décoder)

**Exemple** :
```java
String rawPassword = "myPassword123";
String encoded = passwordEncoder.encode(rawPassword);
// $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

boolean matches = passwordEncoder.matches(rawPassword, encoded);
// true
```

### DelegatingPasswordEncoder

Permet de supporter plusieurs algorithmes.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    String idForEncode = "bcrypt";
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put(idForEncode, new BCryptPasswordEncoder());
    encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
    
    return new DelegatingPasswordEncoder(idForEncode, encoders);
}
```

Format : `{algorithm}encodedPassword`

Exemple : `{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMye...`

---

## 5. Role vs Permission

### Role (Rôle)

**Définition** : Groupe de permissions de haut niveau.

**Exemples** :
- ADMIN
- USER
- MANAGER
- MODERATOR

```java
@Entity
public class Role {
    @Id
    private Long id;
    private String name; // ADMIN, USER
    
    @ManyToMany
    private Set<Permission> permissions;
}
```

### Permission

**Définition** : Action spécifique qu'un utilisateur peut effectuer.

**Exemples** :
- READ_USERS
- WRITE_USERS
- DELETE_USERS
- UPDATE_PROFILE

```java
@Entity
public class Permission {
    @Id
    private Long id;
    private String name; // READ_USERS, WRITE_USERS
}
```

### Hiérarchie

```
ADMIN
  ├── READ_USERS
  ├── WRITE_USERS
  ├── DELETE_USERS
  └── MANAGE_ROLES

USER
  ├── READ_PROFILE
  └── UPDATE_PROFILE
```

### Utilisation

#### Par rôle

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
    // Seuls les ADMIN
}
```

#### Par permission

```java
@PreAuthorize("hasAuthority('DELETE_USERS')")
public void deleteUser(Long id) {
    // Utilisateurs avec permission DELETE_USERS
}
```

#### Combinaison

```java
@PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_USERS')")
public void deleteUser(Long id) {
    // ADMIN ou permission DELETE_USERS
}
```

---

## 6. JWT

### Structure

```
HEADER.PAYLOAD.SIGNATURE
```

#### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

#### Payload (Claims)

```json
{
  "sub": "john@example.com",
  "name": "John Doe",
  "roles": ["USER", "ADMIN"],
  "iat": 1516239022,
  "exp": 1516242622
}
```

#### Signature

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### Claims standards

| Claim | Description |
|-------|-------------|
| **iss** | Issuer (émetteur) |
| **sub** | Subject (sujet/utilisateur) |
| **aud** | Audience (destinataire) |
| **exp** | Expiration time |
| **nbf** | Not before |
| **iat** | Issued at |
| **jti** | JWT ID (unique) |

### Avantages

✅ **Stateless** : Pas de session serveur  
✅ **Scalable** : Fonctionne avec load balancers  
✅ **Cross-domain** : Utilisable sur plusieurs domaines  
✅ **Mobile-friendly** : Idéal pour apps mobiles  

### Inconvénients

❌ **Taille** : Plus gros qu'un session ID  
❌ **Révocation** : Difficile d'invalider un token  
❌ **Sécurité** : Doit être stocké de manière sécurisée  

---

## 7. OAuth2 & OIDC

### OAuth2

**Définition** : Protocole d'**autorisation** déléguée.

**Use case** : "Login with Google", "Login with Facebook"

#### Grant Types

| Type | Usage |
|------|-------|
| **Authorization Code** | Applications web |
| **Implicit** | ❌ Déprécié |
| **Password** | Applications de confiance |
| **Client Credentials** | Machine-to-machine |
| **Refresh Token** | Renouveler access token |

#### Flow Authorization Code

```
1. User -> Client: Click "Login with Google"
2. Client -> Auth Server: Redirect to login
3. User -> Auth Server: Login + Approve
4. Auth Server -> Client: Authorization Code
5. Client -> Auth Server: Exchange code for token
6. Auth Server -> Client: Access Token
7. Client -> Resource Server: Request with token
8. Resource Server -> Client: Protected data
```

### OpenID Connect (OIDC)

**Définition** : OAuth2 + **Authentication**

**Ajout** : ID Token (JWT) contenant les infos utilisateur

```json
{
  "sub": "248289761001",
  "name": "John Doe",
  "email": "john@example.com",
  "email_verified": true,
  "picture": "https://example.com/photo.jpg"
}
```

### Comparaison

| Aspect | OAuth2 | OIDC |
|--------|--------|------|
| **Objectif** | Authorization | Authentication + Authorization |
| **Token** | Access Token | Access Token + ID Token |
| **Info user** | Non standard | Standard (UserInfo endpoint) |
| **Usage** | API access | Login/SSO |

---

## 8. CSRF Protection

### Qu'est-ce que CSRF ?

**Cross-Site Request Forgery** : Attaque qui force un utilisateur authentifié à exécuter des actions non désirées.

### Exemple d'attaque

```html
<!-- Site malveillant -->
<img src="https://bank.com/transfer?to=hacker&amount=1000">
```

Si l'utilisateur est connecté à bank.com, la requête sera exécutée !

### Protection Spring Security

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
);
```

### Quand désactiver CSRF ?

✅ **APIs REST stateless** (avec JWT)  
❌ **Applications web avec sessions**  

```java
// Pour APIs REST
http.csrf(csrf -> csrf.disable());
```

---

## 9. CORS Configuration

### Qu'est-ce que CORS ?

**Cross-Origin Resource Sharing** : Mécanisme qui permet à une page web d'accéder à des ressources d'un autre domaine.

### Configuration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Dans SecurityConfig

```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

---

## 📚 Résumé

| Concept | Objectif | Quand utiliser |
|---------|----------|----------------|
| **Authentication** | Vérifier l'identité | Toujours |
| **Authorization** | Vérifier les permissions | Après authentication |
| **Filter Chain** | Intercepter les requêtes | Automatique |
| **UserDetails** | Représenter un utilisateur | Avec base de données |
| **Password Encoding** | Sécuriser les mots de passe | Toujours |
| **Roles** | Grouper les permissions | Applications simples |
| **Permissions** | Contrôle fin | Applications complexes |
| **JWT** | Authentication stateless | APIs REST |
| **OAuth2** | Déléguer l'authentification | SSO, Login social |
| **CSRF** | Protéger contre attaques | Applications web |
| **CORS** | Autoriser cross-origin | APIs publiques |

---

👉 Consultez les [Bonnes Pratiques](./06-BEST_PRACTICES.md)
