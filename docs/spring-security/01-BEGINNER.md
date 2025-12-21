# 🟢 Niveau Débutant - Fondamentaux de la Sécurité

## 📚 Objectifs d'apprentissage

À la fin de ce niveau, vous serez capable de :
- ✅ Comprendre les concepts de base de la sécurité applicative
- ✅ Différencier Authentication et Authorization
- ✅ Configurer Spring Security dans une application Spring Boot
- ✅ Protéger des endpoints REST
- ✅ Implémenter une authentification en mémoire

---

## 🎯 Concepts Clés

### 1. Qu'est-ce que la sécurité applicative ?

La sécurité applicative protège votre application contre :
- 🚫 Accès non autorisés
- 🚫 Injection de code malveillant
- 🚫 Vol de données sensibles
- 🚫 Attaques CSRF, XSS, etc.

**Exemple réel** : Imaginez une banque en ligne sans sécurité - n'importe qui pourrait accéder à n'importe quel compte !

---

### 2. Authentication vs Authorization

#### 🔑 Authentication (Authentification)
**Question** : "Qui êtes-vous ?"

**Réponse** : Prouver votre identité (login + mot de passe)

```java
// Exemple : L'utilisateur prouve qu'il est "john@example.com"
Authentication auth = new UsernamePasswordAuthenticationToken(
    "john@example.com", 
    "password123"
);
```

#### 🛡️ Authorization (Autorisation)
**Question** : "Que pouvez-vous faire ?"

**Réponse** : Vérifier vos permissions (rôles, droits)

```java
// Exemple : L'utilisateur a-t-il le rôle ADMIN ?
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
    // Seuls les ADMIN peuvent exécuter cette méthode
}
```

#### 📊 Tableau comparatif

| Aspect | Authentication | Authorization |
|--------|----------------|---------------|
| **Question** | Qui êtes-vous ? | Que pouvez-vous faire ? |
| **Moment** | Avant l'accès | Après l'authentification |
| **Méthode** | Login/Password, Token, Biométrie | Rôles, Permissions |
| **Exemple** | Se connecter à Gmail | Accéder aux emails d'un dossier |

---

### 3. HTTP, Sessions, Cookies

#### 🌐 HTTP - Protocole Stateless

HTTP est **sans état** : chaque requête est indépendante.

```
Client                          Serveur
  |                               |
  |------ GET /profile --------->|  ❌ Qui êtes-vous ?
  |                               |
  |<----- 401 Unauthorized -------|
```

#### 🍪 Cookies - Mémoire du navigateur

Les cookies stockent des informations côté client.

```http
Set-Cookie: JSESSIONID=ABC123; HttpOnly; Secure
```

#### 📦 Sessions - Mémoire du serveur

Le serveur stocke les données de session.

```java
// Côté serveur
HttpSession session = request.getSession();
session.setAttribute("user", authenticatedUser);
```

#### 🔄 Stateful vs Stateless

| Stateful (Session) | Stateless (Token) |
|-------------------|-------------------|
| Session stockée sur serveur | Pas de session serveur |
| Cookie JSESSIONID | Token JWT |
| Difficile à scaler | Facile à scaler |
| Utilisé pour web apps | Utilisé pour APIs |

---

### 4. Introduction à Spring Security

#### 📦 Qu'est-ce que Spring Security ?

Framework de sécurité pour applications Spring qui fournit :
- Authentication
- Authorization
- Protection contre les attaques courantes
- Intégration facile avec Spring Boot

#### 🚀 Première configuration

**Étape 1** : Ajouter la dépendance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Étape 2** : Démarrer l'application

```bash
mvn spring-boot:run
```

**Résultat** : Spring Security est activé automatiquement !
- 🔒 Tous les endpoints sont protégés
- 👤 User par défaut : `user`
- 🔑 Password : Affiché dans la console

---

## 💻 Pratique - Configuration de base

### Exemple 1 : Authentification en mémoire

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("user")
            .password("{noop}password")
            .roles("USER")
            .build();
        
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}admin123")
            .roles("ADMIN", "USER")
            .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
}
```

### Exemple 2 : Controller sécurisé

```java
@RestController
@RequestMapping("/api")
public class SecuredController {
    
    @GetMapping("/public/hello")
    public String publicEndpoint() {
        return "Accessible à tous";
    }
    
    @GetMapping("/user/profile")
    public String userProfile(Authentication auth) {
        return "Profil de: " + auth.getName();
    }
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "Dashboard administrateur";
    }
}
```

---

## 🎯 Mini-Projet : API REST Sécurisée

### Objectif
Créer une API REST avec endpoints publics et privés, authentification en mémoire, gestion des rôles USER et ADMIN.

### Tests avec cURL

```bash
# Endpoint public
curl http://localhost:8080/api/public/hello

# Endpoint user avec authentification
curl -u user:password http://localhost:8080/api/user/profile

# Endpoint admin
curl -u admin:admin123 http://localhost:8080/api/admin/dashboard
```

---

## ✅ Checklist de validation

- [ ] Comprendre Authentication vs Authorization
- [ ] Configurer Spring Security
- [ ] Créer des utilisateurs en mémoire
- [ ] Protéger des endpoints par rôles
- [ ] Tester avec HTTP Basic Authentication

---

## ➡️ Prochaine étape

👉 Passez au [Niveau Intermédiaire](./02-INTERMEDIATE.md)
