# 🚀 Plan d'Implémentation Sécurité - Projet Tricol

## 📋 Vue d'Ensemble

Ce document détaille **étape par étape** l'implémentation de la sécurité dans votre projet existant.

**Durée estimée** : 3-4 jours  
**Complexité** : Moyenne  
**Prérequis** : Projet Tricol fonctionnel

---

## ✅ PHASE 1 : Configuration de Base (1h)

### Étape 1.1 : Ajouter les Dépendances Maven

**Fichier** : `pom.xml`

Ajoutez ces dépendances APRÈS la dépendance Liquibase :

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

**Commande** :
```bash
mvn clean install
```

### Étape 1.2 : Créer la Structure des Packages

Créez ces packages dans `src/main/java/com/tricol/stock/` :

```
security/
├── entity/
├── repository/
├── dto/
│   ├── request/
│   └── response/
├── service/
│   └── impl/
├── config/
└── controller/
```

**Commande** :
```bash
mkdir -p src/main/java/com/tricol/stock/security/{entity,repository,dto/{request,response},service/impl,config,controller}
```

---

## ✅ PHASE 2 : Entités de Sécurité (2h)

### Étape 2.1 : Créer UserApp

**Fichier** : `security/entity/UserApp.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApp implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    
    @Column(nullable = false)
    private boolean enabled = false;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleApp> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserPermission> customPermissions = new HashSet<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (RoleApp role : roles) {
            for (Permission permission : role.getDefaultPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        
        for (UserPermission userPerm : customPermissions) {
            String permName = userPerm.getPermission().getName();
            if (userPerm.isGranted()) {
                authorities.add(new SimpleGrantedAuthority(permName));
            } else {
                authorities.removeIf(auth -> auth.getAuthority().equals(permName));
            }
        }
        
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
```

### Étape 2.2 : Créer RoleApp

**Fichier** : `security/entity/RoleApp.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleApp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> defaultPermissions = new HashSet<>();
}
```

### Étape 2.3 : Créer Permission

**Fichier** : `security/entity/Permission.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String resource;
    private String action;
    private String description;
}
```

### Étape 2.4 : Créer UserPermission

**Fichier** : `security/entity/UserPermission.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserApp user;
    
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
    
    @Column(nullable = false)
    private boolean granted;
    
    @ManyToOne
    @JoinColumn(name = "granted_by")
    private UserApp grantedBy;
    
    private LocalDateTime grantedAt;
    
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}
```

### Étape 2.5 : Créer AuditLog

**Fichier** : `security/entity/AuditLog.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserApp user;
    
    private String action;
    private String resource;
    private String resourceId;
    
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    private String ipAddress;
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
```

### Étape 2.6 : Créer RefreshToken

**Fichier** : `security/entity/RefreshToken.java`

```java
package com.tricol.stock.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserApp user;
    
    @Column(nullable = false)
    private Instant expiryDate;
}
```

---

## ✅ PHASE 3 : Repositories (30min)

Créez tous les repositories dans `security/repository/` :

### UserRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserApp, Long> {
    Optional<UserApp> findByUsername(String username);
    Optional<UserApp> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### RoleRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.RoleApp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleApp, Long> {
    Optional<RoleApp> findByName(String name);
}
```

### PermissionRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
```

### UserPermissionRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUserId(Long userId);
}
```

### AuditLogRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
```

### RefreshTokenRepository.java
```java
package com.tricol.stock.security.repository;

import com.tricol.stock.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}
```

---

## ✅ PHASE 4 : DTOs (30min)

### Request DTOs

**LoginRequest.java**
```java
package com.tricol.stock.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}
```

**RegisterRequest.java**
```java
package com.tricol.stock.security.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8)
    private String password;
    
    private String firstName;
    private String lastName;
}
```

**RefreshTokenRequest.java**
```java
package com.tricol.stock.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}
```

### Response DTOs

**AuthResponse.java**
```java
package com.tricol.stock.security.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
}
```

**UserResponse.java**
```java
package com.tricol.stock.security.dto.response;

import lombok.*;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Set<String> roles;
    private Set<String> permissions;
}
```

---

## ✅ PHASE 5 : Services (2h)

### JwtService.java

**Fichier** : `security/service/JwtService.java`

```java
package com.tricol.stock.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secret;
    
    @Value("${jwt.expiration:900000}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails.getUsername(), expiration);
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpiration);
    }
    
    private String buildToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
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

---

**À SUIVRE** : Phase 6-10 dans le prochain message...

---

## 📊 Progression

- [x] Phase 1 : Configuration (1h)
- [x] Phase 2 : Entités (2h)
- [x] Phase 3 : Repositories (30min)
- [x] Phase 4 : DTOs (30min)
- [x] Phase 5 : Services - Partie 1 (JwtService)
- [ ] Phase 6 : Services - Partie 2 (AuthService, UserDetailsService)
- [ ] Phase 7 : Configuration Security
- [ ] Phase 8 : Controllers
- [ ] Phase 9 : Sécurisation des endpoints existants
- [ ] Phase 10 : Tests
- [ ] Phase 11 : Docker
- [ ] Phase 12 : CI/CD

**Temps total estimé** : 3-4 jours
