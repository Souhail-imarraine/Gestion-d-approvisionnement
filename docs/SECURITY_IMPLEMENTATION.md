# 🔐 Implémentation de la Sécurité - Projet Tricol

## 📋 Contexte du Projet

### Situation Actuelle

Suite au développement réussi du module de gestion des approvisionnements et des stocks pour l'entreprise **Tricol**, la direction informatique souhaite maintenant **sécuriser l'accès** à cette application avant sa mise en production.

### Problématique

L'application gère des **données sensibles** :
- 💰 Informations fournisseurs
- 💵 Prix d'achat
- 📊 Valorisation des stocks
- 📦 Mouvements de stock
- 🚚 Commandes et réceptions

Ces données doivent être accessibles à **différents profils d'utilisateurs** au sein de l'entreprise, chacun avec des **permissions spécifiques**.

### Risques Sans Sécurité

❌ **Accès non autorisé** aux données sensibles  
❌ **Modification frauduleuse** des prix ou stocks  
❌ **Vol d'informations** fournisseurs  
❌ **Absence de traçabilité** des actions  
❌ **Non-conformité** réglementaire (RGPD)  

---

## 🎯 Objectifs de Sécurisation

### Objectifs Principaux

1. ✅ **Authentifier les utilisateurs** - Vérifier l'identité avant l'accès
2. ✅ **Gérer les autorisations** - Contrôler l'accès selon les rôles
3. ✅ **Protéger les endpoints** - Sécuriser l'API REST
4. ✅ **Sécuriser les données** - Protéger les informations sensibles
5. ✅ **Tracer les actions** - Audit et conformité

### Bénéfices Attendus

| Bénéfice | Impact |
|----------|--------|
| **Sécurité renforcée** | Protection des données sensibles |
| **Conformité** | Respect des normes de sécurité |
| **Traçabilité** | Audit complet des actions |
| **Flexibilité** | Gestion dynamique des permissions |
| **Scalabilité** | Architecture prête pour la croissance |

---

## 👥 Utilisateurs et Rôles

### Architecture des Rôles

```
┌─────────────────────────────────────────────────────────┐
│                        ADMIN                             │
│  - Gestion complète du système                          │
│  - Attribution des rôles                                │
│  - Personnalisation des permissions                     │
└─────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼──────┐  ┌───────▼────────┐
│ RESPONSABLE    │  │ MAGASINIER  │  │ CHEF_ATELIER   │
│ ACHATS         │  │             │  │                │
│                │  │             │  │                │
│ - Fournisseurs │  │ - Réception │  │ - Bons sortie  │
│ - Commandes    │  │ - Stock     │  │ - Consultation │
│ - Produits     │  │ - Bons      │  │                │
└────────────────┘  └─────────────┘  └────────────────┘
```

### Matrice des Permissions

#### 1. ADMIN (Administrateur Système)

| Ressource | Permissions |
|-----------|-------------|
| **Utilisateurs** | CREATE, READ, UPDATE, DELETE |
| **Rôles** | CREATE, READ, UPDATE, DELETE |
| **Permissions** | CREATE, READ, UPDATE, DELETE, ASSIGN |
| **Fournisseurs** | CREATE, READ, UPDATE, DELETE |
| **Produits** | CREATE, READ, UPDATE, DELETE |
| **Commandes** | CREATE, READ, UPDATE, DELETE |
| **Stock** | READ, UPDATE |
| **Bons de sortie** | CREATE, READ, UPDATE, DELETE |
| **Audit** | READ |

#### 2. RESPONSABLE_ACHATS

| Ressource | Permissions |
|-----------|-------------|
| **Fournisseurs** | CREATE, READ, UPDATE |
| **Produits** | CREATE, READ, UPDATE |
| **Commandes** | CREATE, READ, UPDATE |
| **Stock** | READ |
| **Bons de sortie** | READ |

#### 3. MAGASINIER

| Ressource | Permissions |
|-----------|-------------|
| **Fournisseurs** | READ |
| **Produits** | READ |
| **Commandes** | READ, RECEPTION |
| **Stock** | READ, UPDATE |
| **Bons de sortie** | CREATE, READ |

#### 4. CHEF_ATELIER

| Ressource | Permissions |
|-----------|-------------|
| **Produits** | READ |
| **Stock** | READ |
| **Bons de sortie** | CREATE, READ |

---

## 🔄 Gestion Dynamique des Permissions

### Principe de Fonctionnement

#### Étape 1 : Inscription (Register)

```
Nouvel Utilisateur
       ↓
   S'inscrit
       ↓
Aucun rôle attribué
       ↓
Aucune permission
       ↓
Attend validation ADMIN
```

**Caractéristiques** :
- ✅ Compte créé mais inactif
- ❌ Aucune action possible
- ⏳ En attente d'attribution de rôle

#### Étape 2 : Attribution de Rôle

```
ADMIN
  ↓
Assigne un rôle (ex: MAGASINIER)
  ↓
Utilisateur hérite des permissions par défaut
  ↓
Peut utiliser l'application
```

**Exemple** :
```java
// L'utilisateur Amine reçoit le rôle MAGASINIER
admin.assignRole(amine, "MAGASINIER");

// Amine hérite automatiquement des permissions :
// - READ_FOURNISSEURS
// - READ_PRODUITS
// - READ_COMMANDES
// - RECEPTION_COMMANDES
// - READ_STOCK
// - UPDATE_STOCK
// - CREATE_BON_SORTIE
// - READ_BON_SORTIE
```

#### Étape 3 : Personnalisation des Permissions

```
ADMIN
  ↓
Modifie les permissions individuelles
  ↓
Utilisateur conserve son rôle
  ↓
Mais avec permissions personnalisées
```

**Exemple Concret** :

```
Utilisateur : Amine
Rôle : MAGASINIER

Permissions par défaut :
✅ Réceptionner commandes
✅ Consulter stock
✅ Créer bons de sortie
✅ Mettre à jour stock

Action ADMIN :
❌ Retirer permission "Créer bons de sortie"

Résultat :
✅ Réceptionner commandes
✅ Consulter stock
❌ Créer bons de sortie (RETIRÉ)
✅ Mettre à jour stock
```

### Avantages de ce Système

| Avantage | Description |
|----------|-------------|
| **Flexibilité** | Adaptation aux besoins spécifiques |
| **Sécurité** | Principe du moindre privilège |
| **Évolutivité** | Ajout facile de nouvelles permissions |
| **Traçabilité** | Historique des modifications |
| **Granularité** | Contrôle fin des accès |

---

## 🛠️ Architecture Technique

### Stack Technologique

```
┌─────────────────────────────────────────┐
│         Frontend (React/Angular)        │
└──────────────┬──────────────────────────┘
               │ HTTP + JWT
               ↓
┌─────────────────────────────────────────┐
│      Spring Boot REST API (8080)        │
│  ┌───────────────────────────────────┐  │
│  │      Spring Security + JWT        │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │    Controllers (Secured)          │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │    Services (Business Logic)      │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │    Repositories (JPA)             │  │
│  └───────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         MySQL Database                   │
│  - users                                 │
│  - roles                                 │
│  - permissions                           │
│  - user_permissions                      │
│  - audit_logs                            │
└─────────────────────────────────────────┘
```

### Modèle de Données

```sql
-- Table UserApp
users
├── id (PK)
├── username (UNIQUE)
├── email (UNIQUE)
├── password (HASHED)
├── enabled
├── created_at
└── updated_at

-- Table RoleApp
roles
├── id (PK)
├── name (UNIQUE)
└── description

-- Table Permission
permissions
├── id (PK)
├── name (UNIQUE)
├── resource
└── action

-- Table role_permissions (Many-to-Many)
role_permissions
├── role_id (FK)
└── permission_id (FK)

-- Table user_permissions (Personnalisation)
user_permissions
├── id (PK)
├── user_id (FK)
├── permission_id (FK)
├── granted (BOOLEAN)
├── granted_by (FK -> users)
└── granted_at

-- Table audit_logs
audit_logs
├── id (PK)
├── user_id (FK)
├── action
├── resource
├── resource_id
├── old_value
├── new_value
├── ip_address
└── timestamp
```

---

## 🔐 Concepts de Sécurité Appliqués

### 1. Authentication (Authentification)

**Question** : "Qui êtes-vous ?"

**Implémentation** :
- JWT (JSON Web Token)
- Access Token (15 minutes)
- Refresh Token (7 jours)

**Flow** :
```
1. User -> Login (username + password)
2. Server -> Valide credentials
3. Server -> Génère JWT (Access + Refresh)
4. Client -> Stocke tokens
5. Client -> Envoie Access Token dans chaque requête
6. Server -> Valide token et autorise
```

### 2. Authorization (Autorisation)

**Question** : "Que pouvez-vous faire ?"

**Implémentation** :
- Role-Based Access Control (RBAC)
- Permission-Based Access Control
- Dynamic Permission Override

**Exemple** :
```java
@PreAuthorize("hasPermission('FOURNISSEUR', 'CREATE')")
public Fournisseur createFournisseur(FournisseurDTO dto) {
    // Seuls les utilisateurs avec permission CREATE_FOURNISSEUR
}
```

### 3. Password Security

**Implémentation** :
- BCrypt avec coût 12
- Politique de mot de passe forte
- Pas de stockage en clair

```java
// Hashing
String hashedPassword = passwordEncoder.encode("password123");
// $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Validation
boolean matches = passwordEncoder.matches("password123", hashedPassword);
```

### 4. JWT Security

**Structure** :
```
Header.Payload.Signature

{
  "sub": "amine@tricol.com",
  "roles": ["MAGASINIER"],
  "permissions": ["READ_STOCK", "UPDATE_STOCK"],
  "iat": 1234567890,
  "exp": 1234568790
}
```

**Avantages** :
- ✅ Stateless (pas de session serveur)
- ✅ Scalable
- ✅ Cross-domain
- ✅ Mobile-friendly

### 5. Audit & Traçabilité

**Actions tracées** :
- 🔐 Connexions/Déconnexions
- 👤 Modifications d'utilisateurs
- 🔑 Changements de permissions
- 📝 Création/Modification/Suppression de données
- ❌ Tentatives d'accès non autorisées

**Exemple d'audit log** :
```json
{
  "user": "admin@tricol.com",
  "action": "REMOVE_PERMISSION",
  "resource": "USER_PERMISSION",
  "details": {
    "target_user": "amine@tricol.com",
    "permission": "CREATE_BON_SORTIE",
    "reason": "Restriction temporaire"
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "ip_address": "192.168.1.100"
}
```

---

## 📦 Fonctionnalités à Implémenter

### Phase 1 : Configuration de Base

#### 1.1 Dépendances Maven

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

#### 1.2 Entités

**UserApp** :
```java
@Entity
public class UserApp implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean enabled;
    private Set<RoleApp> roles;
    private Set<UserPermission> customPermissions;
}
```

**RoleApp** :
```java
@Entity
public class RoleApp {
    private Long id;
    private String name; // ADMIN, RESPONSABLE_ACHATS, etc.
    private Set<Permission> defaultPermissions;
}
```

**Permission** :
```java
@Entity
public class Permission {
    private Long id;
    private String name; // CREATE_FOURNISSEUR
    private String resource; // FOURNISSEUR
    private String action; // CREATE
}
```

**UserPermission** :
```java
@Entity
public class UserPermission {
    private Long id;
    private UserApp user;
    private Permission permission;
    private boolean granted; // true = ajouté, false = retiré
    private UserApp grantedBy;
    private LocalDateTime grantedAt;
}
```

### Phase 2 : Authentification

#### 2.1 Endpoints Auth

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

#### 2.2 JWT Service

```java
@Service
public class JwtService {
    String generateToken(UserDetails user);
    String generateRefreshToken(UserDetails user);
    boolean validateToken(String token);
    String extractUsername(String token);
}
```

### Phase 3 : Autorisation

#### 3.1 Sécurisation des Endpoints

```java
@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {
    
    @GetMapping
    @PreAuthorize("hasPermission('FOURNISSEUR', 'READ')")
    public List<FournisseurDTO> getAll() { }
    
    @PostMapping
    @PreAuthorize("hasPermission('FOURNISSEUR', 'CREATE')")
    public FournisseurDTO create(@RequestBody FournisseurDTO dto) { }
}
```

#### 3.2 Custom Permission Evaluator

```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication auth, 
                                 Object resource, 
                                 Object action) {
        // Vérifier permissions par défaut du rôle
        // + permissions personnalisées de l'utilisateur
    }
}
```

### Phase 4 : Gestion des Permissions

#### 4.1 Endpoints Admin

```
POST /api/admin/users/{userId}/roles
POST /api/admin/users/{userId}/permissions/grant
POST /api/admin/users/{userId}/permissions/revoke
GET  /api/admin/users/{userId}/permissions
```

#### 4.2 Service de Gestion

```java
@Service
public class PermissionManagementService {
    
    void assignRole(Long userId, String roleName);
    void grantPermission(Long userId, Long permissionId);
    void revokePermission(Long userId, Long permissionId);
    Set<Permission> getUserEffectivePermissions(Long userId);
}
```

### Phase 5 : Audit

#### 5.1 Audit Interceptor

```java
@Aspect
@Component
public class AuditAspect {
    
    @Around("@annotation(Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) {
        // Log avant
        Object result = joinPoint.proceed();
        // Log après
        return result;
    }
}
```

#### 5.2 Audit Service

```java
@Service
public class AuditService {
    
    void logAction(String action, String resource, Object details);
    void logLogin(String username, boolean success);
    void logPermissionChange(Long userId, Long permissionId, boolean granted);
}
```

---

## 🧪 Tests Unitaires

### Tests d'Authentification

```java
@SpringBootTest
class AuthenticationTests {
    
    @Test
    void testRegister_Success() { }
    
    @Test
    void testLogin_ValidCredentials() { }
    
    @Test
    void testLogin_InvalidCredentials() { }
    
    @Test
    void testRefreshToken_Valid() { }
    
    @Test
    void testRefreshToken_Expired() { }
}
```

### Tests d'Autorisation

```java
@SpringBootTest
class AuthorizationTests {
    
    @Test
    void testAccess_WithPermission() { }
    
    @Test
    void testAccess_WithoutPermission() { }
    
    @Test
    void testCustomPermission_Override() { }
}
```

---

## 🐳 Dockerization

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_DATABASE: tricol_db
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_db
    depends_on:
      - mysql
```

### Commandes Docker

```bash
# Build image
docker build -t tricol-app:latest .

# Run container
docker run -p 8080:8080 tricol-app:latest

# Push to Docker Hub
docker tag tricol-app:latest username/tricol-app:latest
docker push username/tricol-app:latest
```

---

## 🚀 CI/CD avec GitHub Actions

### Workflow

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build with Maven
        run: mvn clean package
      
      - name: Run Tests
        run: mvn test
  
  docker:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and Push
        run: |
          docker build -t ${{ secrets.DOCKER_USERNAME }}/tricol-app:latest .
          docker push ${{ secrets.DOCKER_USERNAME }}/tricol-app:latest
```

### Secrets GitHub

```
DOCKER_USERNAME
DOCKER_PASSWORD
JWT_SECRET
DB_PASSWORD
```

---

## 📊 Résumé des Bénéfices

### Sécurité

✅ **Authentification robuste** avec JWT  
✅ **Autorisation granulaire** par permission  
✅ **Mots de passe sécurisés** avec BCrypt  
✅ **Protection des endpoints** API  
✅ **Audit complet** des actions  

### Flexibilité

✅ **Gestion dynamique** des permissions  
✅ **Personnalisation** par utilisateur  
✅ **Évolutivité** facile  

### Conformité

✅ **Traçabilité** complète  
✅ **RGPD** compatible  
✅ **Normes de sécurité** respectées  

### Production

✅ **Dockerisé** et portable  
✅ **CI/CD** automatisé  
✅ **Tests** unitaires  
✅ **Monitoring** intégré  

---

## 🎯 Conclusion

L'implémentation de Spring Security dans le projet Tricol est **essentielle** pour :

1. **Protéger** les données sensibles de l'entreprise
2. **Contrôler** l'accès selon les rôles et permissions
3. **Tracer** toutes les actions pour l'audit
4. **Respecter** les normes de sécurité
5. **Préparer** l'application pour la production

Cette architecture de sécurité offre une **flexibilité maximale** tout en maintenant un **niveau de sécurité élevé**, permettant à Tricol de gérer efficacement ses approvisionnements en toute confiance.

---

**Prochaines étapes** : Commencer l'implémentation en suivant les phases définies ci-dessus.
