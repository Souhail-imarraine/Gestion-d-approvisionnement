# Documentation Complète - Sécurité Spring Security + JWT

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture de Sécurité](#architecture-de-sécurité)
3. [Concepts Fondamentaux](#concepts-fondamentaux)
4. [Flux d'Authentification](#flux-dauthentification)
5. [Système de Permissions](#système-de-permissions)
6. [Configuration Détaillée](#configuration-détaillée)
7. [Guide d'Utilisation](#guide-dutilisation)
8. [Audit et Traçabilité](#audit-et-traçabilité)

---

## Vue d'ensemble

Ce projet implémente un système de sécurité complet basé sur **Spring Security 6** avec authentification **JWT (JSON Web Token)** stateless. Le système gère l'authentification, l'autorisation basée sur les rôles (RBAC), les permissions dynamiques, et l'audit des actions utilisateurs.

### Technologies Utilisées

- **Spring Security 6.x** - Framework de sécurité
- **JWT (JJWT 0.12.3)** - Tokens d'authentification
- **BCrypt** - Hachage des mots de passe
- **MySQL** - Base de données
- **Liquibase** - Gestion des migrations

---

## Architecture de Sécurité

### Schéma Global

```
Client → Request → JwtAuthenticationFilter → SecurityFilterChain → Controller
                           ↓
                    Validate JWT Token
                           ↓
                    Load UserDetails
                           ↓
                    Check Permissions
```

### Composants Principaux

1. **JwtAuthenticationFilter** - Intercepte les requêtes et valide les tokens JWT
2. **SecurityConfig** - Configure les règles de sécurité
3. **JwtService** - Génère et valide les tokens JWT
4. **CustomUserDetailsService** - Charge les utilisateurs depuis la base de données
5. **AuthService** - Gère l'inscription, connexion, et rafraîchissement des tokens
6. **AuditService** - Enregistre les actions utilisateurs

---

## Concepts Fondamentaux

### 1. JWT (JSON Web Token)

Un JWT est un token auto-contenu qui encode les informations d'authentification.

**Structure d'un JWT:**
```
Header.Payload.Signature
```

**Exemple:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNTMyMDAwMH0.signature
```

**Contenu du Payload:**
```json
{
  "sub": "admin",           // Username
  "iat": 1705320000,        // Issued At
  "exp": 1705320900         // Expiration (15 minutes)
}
```

### 2. Access Token vs Refresh Token

| Type | Durée de vie | Usage | Stockage |
|------|--------------|-------|----------|
| **Access Token** | 15 minutes | Authentifier les requêtes API | Mémoire client |
| **Refresh Token** | 7 jours | Obtenir un nouveau Access Token | Base de données |

**Pourquoi deux tokens?**
- **Sécurité**: Si l'Access Token est compromis, il expire rapidement
- **Expérience utilisateur**: Le Refresh Token évite de redemander le mot de passe

### 3. Stateless Authentication

**Stateless** signifie que le serveur ne stocke pas de session. Chaque requête contient toutes les informations nécessaires (le JWT).

**Avantages:**
- Scalabilité horizontale (load balancing)
- Pas de gestion de sessions côté serveur
- Idéal pour les architectures microservices

### 4. BCrypt Password Hashing

BCrypt est un algorithme de hachage avec **salt** automatique et **cost factor** ajustable.

**Exemple:**
```
Password: "password"
Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**Propriétés:**
- Unidirectionnel (impossible à décrypter)
- Résistant aux attaques par force brute
- Chaque hash est unique même pour le même mot de passe

---

## Flux d'Authentification

### 1. Inscription (Register)

```
Client                    Server                    Database
  |                         |                           |
  |-- POST /auth/register ->|                           |
  |   {username, email,     |                           |
  |    password}            |                           |
  |                         |-- Hash password (BCrypt)->|
  |                         |-- Save user (enabled=false)|
  |                         |                           |
  |<-- 200 OK --------------|                           |
  |   "User registered"     |                           |
```

**Code:**
```java
public MessageResponse register(RegisterRequest request) {
    // 1. Vérifier si l'utilisateur existe
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new RuntimeException("Username already exists");
    }
    
    // 2. Créer l'utilisateur
    UserApp user = new UserApp();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt
    user.setEnabled(false); // Désactivé par défaut
    
    // 3. Sauvegarder
    userRepository.save(user);
    
    return new MessageResponse("User registered successfully");
}
```

**Points clés:**
- Le mot de passe est haché avec BCrypt
- L'utilisateur est désactivé par défaut (enabled=false)
- Un admin doit l'activer et lui assigner un rôle

### 2. Connexion (Login)

```
Client                    Server                    Database
  |                         |                           |
  |-- POST /auth/login ---->|                           |
  |   {username, password}  |                           |
  |                         |-- Load user ------------->|
  |                         |<-- UserDetails ------------|
  |                         |-- Verify password (BCrypt)|
  |                         |-- Generate Access Token   |
  |                         |-- Generate Refresh Token  |
  |                         |-- Save Refresh Token ---->|
  |                         |                           |
  |<-- 200 OK --------------|                           |
  |   {accessToken,         |                           |
  |    refreshToken,        |                           |
  |    expiresIn: 900000}   |                           |
```

**Code:**
```java
public AuthResponse login(LoginRequest request) {
    // 1. Authentifier avec Spring Security
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );
    
    // 2. Charger l'utilisateur
    UserApp user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // 3. Générer les tokens
    String accessToken = jwtService.generateToken(user.getUsername());
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());
    
    // 4. Sauvegarder le refresh token
    RefreshToken refreshTokenEntity = new RefreshToken();
    refreshTokenEntity.setToken(refreshToken);
    refreshTokenEntity.setUser(user);
    refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
    refreshTokenRepository.save(refreshTokenEntity);
    
    // 5. Audit log
    auditService.logAction(user.getUsername(), "LOGIN", "AUTH", null, null, request.getRemoteAddr());
    
    return new AuthResponse(accessToken, refreshToken, "Bearer", 900000L);
}
```

**Points clés:**
- AuthenticationManager vérifie le mot de passe avec BCrypt
- Deux tokens sont générés: Access (15min) et Refresh (7 jours)
- Le Refresh Token est stocké en base de données
- L'action est enregistrée dans l'audit log

### 3. Requête Authentifiée

```
Client                    JwtAuthenticationFilter    Server
  |                              |                      |
  |-- GET /api/produits -------->|                      |
  |   Header: Authorization:     |                      |
  |   Bearer eyJhbGc...          |                      |
  |                              |-- Extract JWT        |
  |                              |-- Validate signature |
  |                              |-- Check expiration   |
  |                              |-- Extract username   |
  |                              |-- Load UserDetails   |
  |                              |-- Set Authentication |
  |                              |                      |
  |                              |-- Forward request -->|
  |                              |                      |
  |<-- 200 OK -------------------|----------------------|
  |   [list of products]         |                      |
```

**Code du filtre:**
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    // 1. Extraire le token du header Authorization
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }
    
    String jwt = authHeader.substring(7);
    
    // 2. Extraire le username du token
    String username = jwtService.extractUsername(jwt);
    
    // 3. Valider le token
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (jwtService.isTokenValid(jwt, userDetails)) {
            // 4. Créer l'authentification
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );
            
            // 5. Définir l'authentification dans le contexte
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    
    // 6. Continuer la chaîne de filtres
    filterChain.doFilter(request, response);
}
```

**Points clés:**
- Le filtre s'exécute avant chaque requête
- Le JWT est extrait du header `Authorization: Bearer <token>`
- Le token est validé (signature + expiration)
- L'authentification est placée dans le SecurityContext

### 4. Rafraîchissement du Token

```
Client                    Server                    Database
  |                         |                           |
  |-- POST /auth/refresh -->|                           |
  |   {refreshToken}        |                           |
  |                         |-- Validate refresh token->|
  |                         |<-- RefreshToken entity ---|
  |                         |-- Check expiration        |
  |                         |-- Generate new Access Token|
  |                         |                           |
  |<-- 200 OK --------------|                           |
  |   {accessToken,         |                           |
  |    refreshToken,        |                           |
  |    expiresIn: 900000}   |                           |
```

**Code:**
```java
public AuthResponse refreshToken(RefreshTokenRequest request) {
    // 1. Trouver le refresh token
    RefreshToken refreshToken = refreshTokenRepository
        .findByToken(request.getRefreshToken())
        .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    
    // 2. Vérifier l'expiration
    if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        refreshTokenRepository.delete(refreshToken);
        throw new RuntimeException("Refresh token expired");
    }
    
    // 3. Générer un nouveau access token
    String newAccessToken = jwtService.generateToken(refreshToken.getUser().getUsername());
    
    return new AuthResponse(newAccessToken, request.getRefreshToken(), "Bearer", 900000L);
}
```

**Points clés:**
- Le Refresh Token est vérifié en base de données
- Si valide, un nouveau Access Token est généré
- Le Refresh Token reste le même (sauf s'il est expiré)

---

## Système de Permissions

### 1. Modèle RBAC (Role-Based Access Control)

Le système utilise un modèle RBAC avec permissions dynamiques.

**Hiérarchie:**
```
User → Roles → Permissions
  ↓
Custom Permissions (override)
```

### 2. Les 4 Rôles

| Rôle | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | Administrateur système | Toutes les permissions |
| **RESPONSABLE_ACHATS** | Gère les achats | Fournisseurs, Produits, Commandes |
| **MAGASINIER** | Gère le stock | Réceptions, Stock, Bons de sortie |
| **CHEF_ATELIER** | Chef d'atelier | Lecture Produits/Stock, Créer Bon sortie |

### 3. Les 19 Permissions

**Format:** `ACTION_RESOURCE`

**Fournisseurs:**
- READ_FOURNISSEUR
- CREATE_FOURNISSEUR
- UPDATE_FOURNISSEUR
- DELETE_FOURNISSEUR

**Produits:**
- READ_PRODUIT
- CREATE_PRODUIT
- UPDATE_PRODUIT
- DELETE_PRODUIT

**Commandes:**
- READ_COMMANDE
- CREATE_COMMANDE
- UPDATE_COMMANDE
- DELETE_COMMANDE
- VALIDATE_COMMANDE

**Réceptions:**
- CREATE_RECEPTION

**Stock:**
- READ_STOCK

**Bons de Sortie:**
- READ_BON_SORTIE
- CREATE_BON_SORTIE
- UPDATE_BON_SORTIE
- DELETE_BON_SORTIE

### 4. Permissions Dynamiques

Un admin peut accorder ou révoquer des permissions individuelles à un utilisateur.

**Exemple:**
```
User: magasinier1
Role: MAGASINIER (permissions par défaut: CREATE_RECEPTION, READ_STOCK, etc.)

Admin révoque: CREATE_BON_SORTIE
→ magasinier1 ne peut plus créer de bons de sortie

Admin accorde: READ_COMMANDE
→ magasinier1 peut maintenant lire les commandes
```

**Schéma de base de données:**
```sql
-- Table de jonction pour permissions personnalisées
CREATE TABLE user_permissions (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    permission_id BIGINT,
    granted BOOLEAN,  -- true = accordé, false = révoqué
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
```

**Logique de résolution:**
```java
public Set<Permission> getUserPermissions(UserApp user) {
    Set<Permission> permissions = new HashSet<>();
    
    // 1. Ajouter les permissions des rôles
    for (RoleApp role : user.getRoles()) {
        permissions.addAll(role.getDefaultPermissions());
    }
    
    // 2. Appliquer les permissions personnalisées
    for (UserPermission userPerm : user.getCustomPermissions()) {
        if (userPerm.isGranted()) {
            permissions.add(userPerm.getPermission()); // Ajouter
        } else {
            permissions.remove(userPerm.getPermission()); // Révoquer
        }
    }
    
    return permissions;
}
```

---

## Configuration Détaillée

### 1. SecurityConfig

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
            // Désactiver CSRF (stateless)
            .csrf(csrf -> csrf.disable())
            
            // Configurer les autorisations
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Endpoints publics
                .anyRequest().authenticated()                // Tout le reste nécessite authentification
            )
            
            // Session stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Ajouter le filtre JWT
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Cost factor par défaut: 10
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**Points clés:**
- **CSRF désactivé**: Pas nécessaire en stateless (pas de cookies)
- **Session stateless**: Pas de JSESSIONID
- **JwtAuthFilter**: Exécuté avant UsernamePasswordAuthenticationFilter

### 2. JwtService

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    // Générer un token
    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    // Extraire le username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Valider le token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**Configuration (application.properties):**
```properties
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000          # 15 minutes
jwt.refresh-expiration=604800000  # 7 jours
```

**Génération de la clé secrète:**
```java
// Générer une clé HS256 (256 bits)
SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
String base64Key = Encoders.BASE64.encode(key.getEncoded());
```

---

## Guide d'Utilisation

### 1. Démarrage Initial

**Étape 1: Démarrer l'application**
```bash
mvn spring-boot:run
```

**Étape 2: Liquibase crée les tables et insère les données initiales**
- 7 tables métier (fournisseurs, produits, etc.)
- 8 tables sécurité (users, roles, permissions, etc.)
- 1 admin par défaut

**Étape 3: Se connecter avec le compte admin**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "password"
}
```

**Réponse:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900000
}
```

### 2. Créer un Nouvel Utilisateur

**Étape 1: Inscription**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "username": "magasinier1",
    "email": "magasinier1@tricol.com",
    "password": "password123"
}
```

**Étape 2: Activer l'utilisateur (SQL - à faire par admin)**
```sql
-- Activer l'utilisateur
UPDATE users SET enabled = true WHERE username = 'magasinier1';

-- Assigner le rôle MAGASINIER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'magasinier1' AND r.name = 'MAGASINIER';
```

**Étape 3: L'utilisateur peut se connecter**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "magasinier1",
    "password": "password123"
}
```

### 3. Utiliser l'API

**Toutes les requêtes nécessitent le header Authorization:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Exemple avec curl:**
```bash
curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Exemple avec Postman:**
1. Importer la collection `Tricol_Stock_Management.postman_collection.json`
2. Exécuter "Login Admin"
3. Le token est automatiquement sauvegardé dans la variable `{{accessToken}}`
4. Toutes les autres requêtes utilisent automatiquement ce token

### 4. Gérer les Permissions

**Révoquer une permission:**
```sql
-- Empêcher magasinier1 de créer des bons de sortie
INSERT INTO user_permissions (user_id, permission_id, granted)
SELECT u.id, p.id, false
FROM users u, permissions p
WHERE u.username = 'magasinier1' AND p.name = 'CREATE_BON_SORTIE';
```

**Accorder une permission:**
```sql
-- Permettre à chef_atelier1 de créer des commandes
INSERT INTO user_permissions (user_id, permission_id, granted)
SELECT u.id, p.id, true
FROM users u, permissions p
WHERE u.username = 'chef_atelier1' AND p.name = 'CREATE_COMMANDE';
```

---

## Audit et Traçabilité

### 1. Table audit_logs

Chaque action importante est enregistrée:

```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50),
    action VARCHAR(50),      -- LOGIN, CREATE, UPDATE, DELETE
    resource VARCHAR(100),   -- AUTH, FOURNISSEUR, PRODUIT, etc.
    resource_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP
);
```

### 2. Actions Auditées

- **LOGIN** - Connexion utilisateur
- **LOGOUT** - Déconnexion
- **CREATE** - Création d'entité
- **UPDATE** - Modification
- **DELETE** - Suppression
- **VALIDATE** - Validation de commande
- **RECEPTION** - Réception de commande

### 3. Exemple d'Audit Log

```java
@PostMapping("/fournisseurs")
public FournisseurResponseDTO create(@RequestBody FournisseurRequestDTO dto) {
    FournisseurResponseDTO created = fournisseurService.create(dto);
    
    // Audit log
    auditService.logAction(
        getCurrentUsername(),
        "CREATE",
        "FOURNISSEUR",
        created.getId(),
        null,
        dto.toString(),
        request.getRemoteAddr()
    );
    
    return created;
}
```

### 4. Consulter les Logs

```sql
-- Toutes les actions d'un utilisateur
SELECT * FROM audit_logs 
WHERE username = 'magasinier1' 
ORDER BY timestamp DESC;

-- Toutes les modifications d'un fournisseur
SELECT * FROM audit_logs 
WHERE resource = 'FOURNISSEUR' AND resource_id = 1
ORDER BY timestamp DESC;

-- Connexions des dernières 24h
SELECT * FROM audit_logs 
WHERE action = 'LOGIN' 
AND timestamp > NOW() - INTERVAL 1 DAY;
```

---

## Résumé des Concepts Clés

1. **JWT Stateless**: Pas de session serveur, scalabilité maximale
2. **Access + Refresh Tokens**: Sécurité (courte durée) + UX (pas de re-login)
3. **BCrypt**: Hachage sécurisé des mots de passe
4. **RBAC**: Rôles avec permissions par défaut
5. **Permissions Dynamiques**: Override des permissions par utilisateur
6. **Audit Trail**: Traçabilité complète des actions
7. **Filter Chain**: JwtAuthenticationFilter → SecurityFilterChain → Controller

---

## Diagramme de Séquence Complet

```
┌──────┐         ┌────────┐         ┌──────────┐         ┌──────────┐
│Client│         │ Filter │         │  Service │         │ Database │
└──┬───┘         └───┬────┘         └────┬─────┘         └────┬─────┘
   │                 │                   │                    │
   │ POST /login     │                   │                    │
   ├────────────────>│                   │                    │
   │                 │ authenticate()    │                    │
   │                 ├──────────────────>│                    │
   │                 │                   │ findByUsername()   │
   │                 │                   ├───────────────────>│
   │                 │                   │<───────────────────┤
   │                 │                   │ verify password    │
   │                 │                   │ generate JWT       │
   │                 │                   │ save refresh token │
   │                 │                   ├───────────────────>│
   │                 │<──────────────────┤                    │
   │<────────────────┤                   │                    │
   │ {accessToken}   │                   │                    │
   │                 │                   │                    │
   │ GET /produits   │                   │                    │
   │ Bearer token    │                   │                    │
   ├────────────────>│                   │                    │
   │                 │ validate JWT      │                    │
   │                 │ extract username  │                    │
   │                 │ loadUserDetails() │                    │
   │                 ├──────────────────>│                    │
   │                 │                   │ findByUsername()   │
   │                 │                   ├───────────────────>│
   │                 │                   │<───────────────────┤
   │                 │<──────────────────┤                    │
   │                 │ set authentication│                    │
   │                 │ forward request   │                    │
   │                 ├──────────────────>│                    │
   │                 │                   │ findAll()          │
   │                 │                   ├───────────────────>│
   │                 │                   │<───────────────────┤
   │<────────────────┴───────────────────┤                    │
   │ [products]      │                   │                    │
```

---

**Fin de la documentation**
