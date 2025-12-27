# 🚀 ROADMAP DU PROJET - SÉCURISATION & DEVOPS

## 📋 TABLE DES MATIÈRES

1. [Vue d'ensemble du projet](#vue-densemble-du-projet)
2. [Concepts à maîtriser AVANT de commencer](#concepts-à-maîtriser-avant-de-commencer)
3. [Architecture cible](#architecture-cible)
4. [Division des tâches](#division-des-tâches)
5. [Plan d'implémentation détaillé](#plan-dimplémentation-détaillé)
6. [Ressources d'apprentissage](#ressources-dapprentissage)

---

## 🎯 VUE D'ENSEMBLE DU PROJET

### Situation Actuelle
✅ **Ce qui est DÉJÀ fait:**
- Application Spring Boot fonctionnelle (gestion approvisionnement)
- Authentification JWT locale basique
- Base de données MySQL avec Liquibase
- Système de permissions basé sur les rôles (RBAC)
- API REST avec contrôle d'accès par permissions
- Audit logging de base

### Objectifs du Projet
🎯 **Ce qu'il faut AJOUTER:**

#### 1. **Sécurité Avancée**
- ✅ Authentification locale JWT (DÉJÀ FAIT)
- ❌ Authentification OAuth2 avec Keycloak (SSO)
- ❌ Dual authentication (local + OAuth2)
- ❌ Configuration CORS avancée
- ❌ Tests de sécurité complets

#### 2. **Infrastructure DevOps**
- ❌ Dockerisation complète (app + MySQL + Keycloak)
- ❌ Docker Compose orchestration
- ❌ Pipeline Jenkins CI/CD
- ❌ SonarQube pour qualité du code
- ❌ Automatisation des tests

#### 3. **Tests**
- ❌ Tests unitaires (couverture 80%+)
- ❌ Tests d'intégration OAuth2
- ❌ Tests de sécurité des endpoints

---

## 📚 CONCEPTS À MAÎTRISER AVANT DE COMMENCER

### 🔴 NIVEAU 1 - FONDAMENTAUX (OBLIGATOIRE)

#### A. Spring Security - Concepts Core

##### 1. **Filter Chain & Security Context**
```
📖 Concepts clés:
- Comment fonctionne la chaîne de filtres de sécurité
- SecurityContextHolder et SecurityContext
- Authentication vs Authorization
- UserDetails et UserDetailsService
- GrantedAuthority et permissions

📝 Points importants:
- Ordre d'exécution des filtres
- Différence entre authentication et authorization
- Comment Spring Security stocke les infos de l'utilisateur
- Cycle de vie d'une requête sécurisée

🔗 À étudier dans votre projet:
- JwtAuthenticationFilter (déjà implémenté)
- SecurityConfig (déjà implémenté)
- CustomUserDetailsService (déjà implémenté)
```

##### 2. **JWT (JSON Web Tokens)**
```
📖 Concepts clés:
- Structure d'un JWT (Header, Payload, Signature)
- Claims standards et custom claims
- Différence entre Access Token et Refresh Token
- Signature et validation des tokens
- Expiration et renouvellement

📝 Points importants:
- Pourquoi JWT est "stateless"
- Sécurité: où stocker les secrets
- Rotation des tokens
- Gestion de l'expiration

🔗 À étudier dans votre projet:
- JwtService.java (déjà implémenté)
- Configuration jwt.* dans application.properties
```

##### 3. **OAuth2 & OpenID Connect**
```
📖 Concepts clés:
- Qu'est-ce qu'OAuth2 et à quoi ça sert
- Les 4 flux OAuth2 (Authorization Code, Implicit, Client Credentials, Password)
- Différence entre OAuth2 et OpenID Connect
- Rôles: Resource Owner, Client, Authorization Server, Resource Server
- Scopes et permissions

📝 Points importants:
- OAuth2 n'est PAS un protocole d'authentification (c'est de l'autorisation)
- OpenID Connect = OAuth2 + Authentication
- Différence entre scope et permission
- Qu'est-ce qu'un ID Token vs Access Token

📚 À apprendre:
- RFC 6749 (OAuth 2.0)
- OpenID Connect Core 1.0
- Les flux en détail (surtout Authorization Code Flow)
```

##### 4. **Keycloak**
```
📖 Concepts clés:
- Qu'est-ce que Keycloak (Identity & Access Management)
- Realms, Clients, Users, Roles
- Client types: Confidential vs Public
- Flow de connexion avec Keycloak
- Token endpoint, Authorization endpoint, UserInfo endpoint

📝 Points importants:
- Comment configurer un Realm
- Créer un client OAuth2
- Mapper les rôles Keycloak vers Spring Security
- Configuration des redirections

📚 À apprendre:
- Installation et configuration de Keycloak
- Création de realms et clients
- Intégration avec Spring Boot
```

#### B. Spring Security OAuth2 Resource Server

##### 5. **Resource Server Configuration**
```
📖 Concepts clés:
- Qu'est-ce qu'un Resource Server
- JWT Decoder et validation des tokens
- JwtAuthenticationConverter
- Extraction des authorities depuis le JWT
- Configuration des issuer-uri

📝 Points importants:
- Comment Spring valide un JWT venant de Keycloak
- Mapping des claims vers authorities
- Configuration du JWK Set URI
- Gestion des scopes vs roles

📚 Dépendances Maven:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

##### 6. **Dual Authentication (Local + OAuth2)**
```
📖 Concepts clés:
- Multiple AuthenticationProvider
- AuthenticationManagerBuilder
- Conditional authentication (local vs OAuth2)
- Token introspection vs JWT validation

📝 Points importants:
- Comment supporter 2 méthodes d'authentification
- Priorité entre les providers
- Gestion des endpoints publics/privés
```

### 🟡 NIVEAU 2 - DEVOPS & INFRASTRUCTURE

#### C. Docker & Conteneurisation

##### 7. **Docker Basics**
```
📖 Concepts clés:
- Images vs Containers
- Dockerfile: FROM, RUN, COPY, EXPOSE, CMD, ENTRYPOINT
- Layers et cache
- Volumes et persistence des données
- Networks et communication inter-containers

📝 Points importants:
- Multi-stage builds pour optimiser la taille
- .dockerignore
- Best practices pour les images Java
- Différence entre CMD et ENTRYPOINT

📚 À pratiquer:
- Créer un Dockerfile pour l'app Spring Boot
- Créer un Dockerfile.dev pour le développement
- Optimiser la taille de l'image
```

##### 8. **Docker Compose**
```
📖 Concepts clés:
- Format docker-compose.yml
- Services, networks, volumes
- Dépendances entre services (depends_on)
- Variables d'environnement
- Health checks

📝 Points importants:
- Orchestration de plusieurs conteneurs
- Gestion des ports et exposition
- Ordre de démarrage des services
- Gestion des secrets

📚 Services à configurer:
- MySQL (base de données)
- Spring Boot App (backend)
- Keycloak (auth server)
- Jenkins (CI/CD)
- SonarQube (qualité code)
```

#### D. CI/CD avec Jenkins

##### 9. **Jenkins Pipeline**
```
📖 Concepts clés:
- Jenkinsfile (Declarative vs Scripted)
- Stages et Steps
- Agents et executors
- Workspace et artifacts
- Triggers (webhooks, polling)

📝 Stages typiques:
1. Checkout (Git)
2. Build (Maven)
3. Test (JUnit)
4. SonarQube Analysis
5. Build Docker Image
6. Push to Registry
7. Deploy

📚 À apprendre:
- Syntaxe Jenkinsfile
- Integration avec Maven
- Docker-in-Docker
- Credentials management
```

##### 10. **SonarQube**
```
📖 Concepts clés:
- Quality Gates
- Code coverage
- Code smells, bugs, vulnerabilities
- Technical debt
- Sonar Scanner pour Maven

📝 Points importants:
- Configuration du plugin Maven
- Seuils de qualité (80% coverage minimum)
- Integration avec Jenkins
- Analyse des rapports

📚 Configuration:
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
</plugin>
```

### 🟢 NIVEAU 3 - TESTS

#### E. Tests Unitaires et d'Intégration

##### 11. **JUnit 5 & Mockito**
```
📖 Concepts clés:
- @Test, @BeforeEach, @AfterEach
- Assertions (assertEquals, assertThrows, etc.)
- @Mock, @InjectMocks
- Mockito: when().thenReturn(), verify()
- ArgumentCaptor

📝 Points importants:
- Différence entre Mock et Spy
- Testing des services avec mocks
- Vérification des interactions
```

##### 12. **Spring Boot Test**
```
📖 Concepts clés:
- @SpringBootTest vs @WebMvcTest
- @MockBean
- MockMvc pour tester les controllers
- TestRestTemplate
- @DataJpaTest pour les repositories

📝 Points importants:
- Test slices (tester uniquement une couche)
- Configuration de test
- Profils de test
```

##### 13. **Tests de Sécurité**
```
📖 Concepts clés:
- @WithMockUser
- @WithUserDetails
- MockMvc avec authentification
- Testing JWT validation
- Testing permissions/roles

📝 Tests à implémenter:
- Test authentification réussie/échouée
- Test accès avec/sans permissions
- Test token expiré
- Test CORS configuration
```

---

## 🏗️ ARCHITECTURE CIBLE

```
┌─────────────────────────────────────────────────────────────┐
│                     DOCKER COMPOSE NETWORK                   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │   Jenkins    │  │  SonarQube   │  │     MySQL       │  │
│  │   (CI/CD)    │  │  (Quality)   │  │  (Database)     │  │
│  │   :8080      │  │   :9000      │  │    :3306        │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
│                                                              │
│  ┌──────────────┐  ┌──────────────────────────────────┐   │
│  │  Keycloak    │  │   Spring Boot App                │   │
│  │  (Auth/SSO)  │◄─┤   - JWT Auth (local)            │   │
│  │   :8180      │  │   - OAuth2 Resource Server       │   │
│  └──────────────┘  │   - API REST                     │   │
│                     │   :8081                          │   │
│                     └──────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    AUTHENTICATION FLOWS                      │
│                                                              │
│  Flow 1: LOCAL JWT                                          │
│  Client → POST /api/v1/auth/login → JWT Token              │
│                                                              │
│  Flow 2: OAUTH2 + KEYCLOAK                                  │
│  Client → Keycloak (login) → Access Token → API            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 DIVISION DES TÂCHES

### PHASE 1: APPRENTISSAGE DES CONCEPTS (2-3 semaines)
**Priorité: HAUTE** ⚠️

#### Semaine 1: Security Fundamentals
- [ ] Étudier OAuth2 et OpenID Connect (théorie)
- [ ] Comprendre le flow Authorization Code
- [ ] Étudier l'architecture Resource Server
- [ ] Lire la documentation Keycloak
- [ ] Suivre un tutoriel Spring Security OAuth2

**Ressources:**
- Documentation officielle Spring Security OAuth2
- Keycloak documentation (Getting Started)
- Video: "OAuth 2.0 and OpenID Connect" (Nate Barbettini)

#### Semaine 2: DevOps Tools
- [ ] Apprendre Docker (images, containers, Dockerfile)
- [ ] Maîtriser Docker Compose
- [ ] Étudier Jenkins Pipeline basics
- [ ] Comprendre SonarQube et quality gates

**Ressources:**
- Docker official tutorial
- Jenkins Pipeline documentation
- SonarQube for Java projects

#### Semaine 3: Testing
- [ ] Réviser JUnit 5 et Mockito
- [ ] Apprendre @SpringBootTest et MockMvc
- [ ] Étudier les tests de sécurité Spring
- [ ] Comprendre les mocks de JWT

**Ressources:**
- Spring Boot Testing documentation
- Baeldung: Testing in Spring Boot

---

### PHASE 2: CONFIGURATION KEYCLOAK (1 semaine)
**Priorité: HAUTE** 🔴

#### Tâche 2.1: Installation Keycloak
- [ ] Créer un Dockerfile pour Keycloak
- [ ] Ajouter Keycloak au docker-compose.yml
- [ ] Configurer les ports et variables d'environnement
- [ ] Tester le démarrage de Keycloak

**Fichiers concernés:**
```
docker-compose.yml (à modifier)
```

**Configuration Docker Compose:**
```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: mysql
      KC_DB_URL: jdbc:mysql://mysql:3306/keycloak
      KC_DB_USERNAME: root
      KC_DB_PASSWORD: password
    ports:
      - "8180:8080"
    depends_on:
      - mysql
    command: start-dev
```

#### Tâche 2.2: Configuration du Realm
- [ ] Créer un realm "tricol-realm"
- [ ] Créer un client "tricol-stock-app"
  - Type: confidential
  - Valid Redirect URIs: http://localhost:8081/*
  - Web Origins: http://localhost:8081
- [ ] Configurer les Client Scopes
- [ ] Créer les rôles (ADMIN, MANAGER, MAGASINIER, VIEWER)

**Détails techniques:**
```
Realm: tricol-realm
Client ID: tricol-stock-app
Client Protocol: openid-connect
Access Type: confidential
Standard Flow Enabled: ON
Direct Access Grants Enabled: ON
```

#### Tâche 2.3: Création des utilisateurs de test
- [ ] Créer 4 utilisateurs (un par rôle)
- [ ] Assigner les rôles
- [ ] Tester la connexion Keycloak

**Utilisateurs de test:**
```
1. admin@tricol.com (ADMIN)
2. manager@tricol.com (MANAGER)
3. magasinier@tricol.com (MAGASINIER)
4. viewer@tricol.com (VIEWER)
Password: Test@123
```

---

### PHASE 3: INTÉGRATION OAUTH2 DANS L'APPLICATION (2 semaines)
**Priorité: HAUTE** 🔴

#### Tâche 3.1: Ajouter les dépendances Maven
**Fichier:** `pom.xml`

```xml
<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- OAuth2 Client (optionnel pour dual auth) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

#### Tâche 3.2: Configuration application.properties
**Fichier:** `src/main/resources/application.properties`

```properties
# OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/tricol-realm/protocol/openid-connect/certs

# OAuth2 Client (pour dual auth)
spring.security.oauth2.client.registration.keycloak.client-id=tricol-stock-app
spring.security.oauth2.client.registration.keycloak.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email

spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8180/realms/tricol-realm
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
```

#### Tâche 3.3: Créer JwtAuthenticationConverter
**Nouveau fichier:** `config/JwtAuthConverter.java`

```java
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extraire les rôles depuis les claims Keycloak
        // Mapper vers les permissions de l'application
    }
}
```

**Concepts importants:**
- Extraction des claims "realm_access.roles"
- Mapping Keycloak roles → Spring authorities
- Préfixe "ROLE_" vs permissions sans préfixe

#### Tâche 3.4: Modifier SecurityConfig pour Dual Auth
**Fichier:** `config/SecurityConfig.java`

**Changements nécessaires:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        // Support JWT local
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        // Support OAuth2 Resource Server
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthConverter)
            )
        );
    
    return http.build();
}
```

**Concepts clés:**
- Dual authentication: JWT local + OAuth2
- Ordre des filtres
- Priorité entre les méthodes d'auth

#### Tâche 3.5: Créer un endpoint OAuth2 pour tester
**Nouveau fichier:** `controller/OAuth2TestController.java`

```java
@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2TestController {
    
    @GetMapping("/user-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        // Retourner les infos de l'utilisateur connecté
        // Supporter JWT local et OAuth2
    }
}
```

---

### PHASE 4: TESTS DE SÉCURITÉ (2 semaines)
**Priorité: HAUTE** 🔴

#### Tâche 4.1: Tests JWT Local (déjà partiellement fait)
**Fichier:** `src/test/java/com/tricol/stock/auth/AuthenticationTests.java`

**Tests à implémenter:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {
    
    @Test
    void testGetAccessTokenFail() {
        // Test avec identifiants incorrects
        // Vérifier 401 Unauthorized
    }
    
    @Test
    void testAccessTokenSuccess() {
        // Test avec identifiants corrects
        // Vérifier 200 OK + présence du token
    }
}
```

#### Tâche 4.2: Tests OAuth2 Resource Server
**Nouveau fichier:** `src/test/java/com/tricol/stock/auth/OAuth2ResourceServerTests.java`

**Tests à implémenter:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2ResourceServerTests {
    
    @Test
    void testAccessWithValidKeycloakToken() {
        // Simuler un token Keycloak valide
        // Tester l'accès à un endpoint protégé
    }
    
    @Test
    void testAccessWithInvalidKeycloakToken() {
        // Token expiré ou invalide
        // Vérifier 401
    }
    
    @Test
    void testRoleMappingFromKeycloak() {
        // Vérifier que les rôles Keycloak sont bien mappés
    }
}
```

#### Tâche 4.3: Tests des Endpoints Produits
**Fichier:** `src/test/java/com/tricol/stock/controller/ProduitControllerTests.java`

**Tests requis (selon le contexte):**
```java
@WebMvcTest(ProduitController.class)
class ProduitControllerTests {
    
    @Test
    @WithMockUser(authorities = "READ_PRODUIT")
    void testListProductWithPermissionRead() {
        // GET /api/v1/produits
        // Vérifier 200 OK
    }
    
    @Test
    @WithMockUser(authorities = "READ_PRODUIT")
    void testProductWithPermissionRead() {
        // GET /api/v1/produits/{id}
        // Vérifier 200 OK + données produit
    }
    
    @Test
    @WithMockUser(authorities = "CREATE_PRODUIT")
    void testAddProductWithPermissionWrite() {
        // POST /api/v1/produits
        // Vérifier 201 Created
    }
    
    @Test
    @WithMockUser(authorities = "READ_PRODUIT")
    void testAddProductWithPermissionRead() {
        // POST /api/v1/produits avec seulement READ
        // Vérifier 403 Forbidden
    }
}
```

**Concepts importants:**
- @WithMockUser vs @WithUserDetails
- MockMvc pour tester les controllers
- Vérification des status HTTP
- Vérification du JSON de réponse

#### Tâche 4.4: Tests de Couverture
- [ ] Configurer JaCoCo pour mesurer la couverture
- [ ] Atteindre 80% de couverture minimum
- [ ] Configurer le rapport de couverture

**Configuration Maven (pom.xml):**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

### PHASE 5: DOCKERISATION COMPLÈTE (1 semaine)
**Priorité: MOYENNE** 🟡

#### Tâche 5.1: Optimiser le Dockerfile de l'application
**Fichier:** `Dockerfile`

**Dockerfile multi-stage optimisé:**
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Avantages:**
- Image finale plus légère (JRE seulement)
- Cache des dépendances Maven
- Sécurité (Alpine Linux)

#### Tâche 5.2: Compléter docker-compose.yml
**Fichier:** `docker-compose.yml`

**Services à ajouter/configurer:**
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: tricol_stock_db
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: mysql
      KC_DB_URL: jdbc:mysql://mysql:3306/keycloak
      KC_DB_USERNAME: root
      KC_DB_PASSWORD: rootpassword
    ports:
      - "8180:8080"
    depends_on:
      mysql:
        condition: service_healthy
    command: start-dev

  app:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/tricol-realm
    ports:
      - "8081:8081"
    depends_on:
      mysql:
        condition: service_healthy
      keycloak:
        condition: service_started

  jenkins:
    image: jenkins/jenkins:lts
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins-data:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      JAVA_OPTS: "-Djenkins.install.runSetupWizard=false"

  sonarqube:
    image: sonarqube:community
    ports:
      - "9000:9000"
    environment:
      SONAR_JDBC_URL: jdbc:mysql://mysql:3306/sonarqube?useUnicode=true&characterEncoding=utf8
      SONAR_JDBC_USERNAME: root
      SONAR_JDBC_PASSWORD: rootpassword
    depends_on:
      mysql:
        condition: service_healthy
    volumes:
      - sonarqube-data:/opt/sonarqube/data

volumes:
  mysql-data:
  jenkins-data:
  sonarqube-data:
```

**Points importants:**
- Health checks pour les dépendances
- Volumes pour la persistence
- Network isolation (automatique avec compose)
- Variables d'environnement

#### Tâche 5.3: Script de démarrage
**Nouveau fichier:** `start.sh` (Linux/Mac) ou `start.bat` (Windows)

```bash
#!/bin/bash
echo "🚀 Démarrage de l'infrastructure Tricol Stock..."

# Arrêter les containers existants
docker-compose down

# Construire les images
docker-compose build

# Démarrer tous les services
docker-compose up -d

# Afficher les logs
docker-compose logs -f app
```

---

### PHASE 6: PIPELINE JENKINS CI/CD (2 semaines)
**Priorité: MOYENNE** 🟡

#### Tâche 6.1: Créer le Jenkinsfile
**Nouveau fichier:** `Jenkinsfile`

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        DOCKER_IMAGE = 'tricol-stock-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'YOUR_GIT_REPO'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=tricol-stock \
                          -Dsonar.host.url=${SONAR_HOST_URL}
                    '''
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.build("${DOCKER_IMAGE}:latest")
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    docker-compose down
                    docker-compose up -d app
                '''
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline exécuté avec succès!'
        }
        failure {
            echo '❌ Le pipeline a échoué.'
        }
        always {
            cleanWs()
        }
    }
}
```

**Concepts clés:**
- Stages séquentiels
- Post-build actions
- Integration SonarQube
- Quality Gate obligatoire
- Docker build et deploy

#### Tâche 6.2: Configuration Jenkins
- [ ] Installer les plugins nécessaires:
  - Git Plugin
  - Maven Integration
  - Docker Pipeline
  - SonarQube Scanner
  - JaCoCo Plugin
- [ ] Configurer les credentials (Git, SonarQube)
- [ ] Créer un job Pipeline
- [ ] Configurer les webhooks Git (optionnel)

#### Tâche 6.3: Configuration SonarQube
- [ ] Accéder à SonarQube (http://localhost:9000)
- [ ] Créer un projet "tricol-stock"
- [ ] Générer un token
- [ ] Configurer le Quality Gate:
  - Coverage: >= 80%
  - Duplications: < 3%
  - Security Hotspots: 0
  - Bugs: 0

**Quality Gate custom:**
```
Conditions:
- Coverage on New Code: >= 80%
- Duplicated Lines on New Code: <= 3%
- Maintainability Rating on New Code: A
- Reliability Rating on New Code: A
- Security Rating on New Code: A
```

---

### PHASE 7: CONFIGURATION CORS (2 jours)
**Priorité: BASSE** 🟢

#### Tâche 7.1: Configuration CORS dans Spring
**Nouveau fichier:** `config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // Frontend local
            "http://localhost:4200",  // Angular local
            "https://app.tricol.com"  // Production
        ));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));
        
        // Exposer certains headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization"
        ));
        
        // Permettre les credentials
        configuration.setAllowCredentials(true);
        
        // Durée de cache de la config CORS
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
```

#### Tâche 7.2: Intégrer CORS dans SecurityConfig
**Fichier:** `config/SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        // ... reste de la configuration
}
```

---

### PHASE 8: DOCUMENTATION & FINALISATION (1 semaine)
**Priorité: MOYENNE** 🟡

#### Tâche 8.1: Documentation API avec Swagger
**Dépendance Maven:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Configuration:**
```java
@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Tricol Stock Management API")
                .version("1.0")
                .description("API de gestion des approvisionnements et stocks"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

#### Tâche 8.2: Guide de déploiement
**Nouveau fichier:** `docs/DEPLOYMENT_GUIDE.md`

Contenu:
- Instructions de déploiement
- Configuration des variables d'environnement
- Procédure de rollback
- Monitoring et logs

#### Tâche 8.3: Guide utilisateur
**Nouveau fichier:** `docs/USER_GUIDE.md`

Contenu:
- Comment s'authentifier (local vs OAuth2)
- Comment utiliser l'API
- Exemples de requêtes
- Gestion des rôles et permissions

---

## 📅 PLANNING GLOBAL

### Timeline Recommandée (12 semaines)

```
Semaines 1-3: APPRENTISSAGE
├── Semaine 1: Security (OAuth2, Keycloak)
├── Semaine 2: DevOps (Docker, Jenkins, SonarQube)
└── Semaine 3: Testing (JUnit, MockMvc)

Semaines 4-5: KEYCLOAK
├── Installation et configuration
├── Création du realm et des clients
└── Tests de connexion

Semaines 6-7: OAUTH2 INTEGRATION
├── Configuration Spring OAuth2 Resource Server
├── Dual authentication
└── Tests OAuth2

Semaines 8-9: TESTS
├── Tests unitaires (80% coverage)
├── Tests d'intégration
└── Tests de sécurité

Semaine 10: DOCKERISATION
├── Optimisation Dockerfile
├── Docker Compose complet
└── Tests de l'infrastructure

Semaines 11-12: CI/CD & FINALISATION
├── Pipeline Jenkins
├── SonarQube integration
├── Documentation
└── Tests finaux
```

---

## 🎓 RESSOURCES D'APPRENTISSAGE

### Documentation Officielle
1. **Spring Security OAuth2**
   - https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html

2. **Keycloak**
   - https://www.keycloak.org/documentation

3. **Docker**
   - https://docs.docker.com/get-started/

4. **Jenkins**
   - https://www.jenkins.io/doc/book/pipeline/

5. **SonarQube**
   - https://docs.sonarqube.org/latest/

### Tutoriels Recommandés

#### OAuth2 & Keycloak
```
📺 Videos:
- "OAuth 2.0 and OpenID Connect (in plain English)" - Nate Barbettini
- "Spring Security OAuth2 Tutorial" - Amigoscode
- "Keycloak Tutorial for Beginners" - TechWorld with Nana

📖 Articles:
- Baeldung: "Spring Security and Keycloak"
- Baeldung: "Spring Security OAuth2 Resource Server"
```

#### Docker & Jenkins
```
📺 Videos:
- "Docker Tutorial for Beginners" - TechWorld with Nana
- "Jenkins Pipeline Tutorial" - DevOps Journey

📖 Articles:
- "Multi-stage Docker builds for Java"
- "Jenkins Declarative Pipeline Syntax"
```

#### Testing
```
📖 Articles:
- Baeldung: "Testing in Spring Boot"
- Baeldung: "Testing Spring Security"
- "MockMvc vs WebTestClient vs TestRestTemplate"
```

### Livres Recommandés
1. **"Spring Security in Action"** - Laurentiu Spilca
2. **"OAuth 2 in Action"** - Justin Richer & Antonio Sanso
3. **"Docker Deep Dive"** - Nigel Poulton

---

## ✅ CHECKLIST FINALE

### Avant de commencer le développement
- [ ] J'ai compris le concept d'OAuth2 et OpenID Connect
- [ ] Je sais ce qu'est un Authorization Server vs Resource Server
- [ ] Je comprends le flow Authorization Code
- [ ] J'ai installé et testé Keycloak localement
- [ ] Je maîtrise Docker et Docker Compose
- [ ] Je connais la syntaxe des Jenkinsfile
- [ ] Je sais écrire des tests unitaires avec JUnit 5 et Mockito

### Critères de validation du projet
- [ ] ✅ Authentification locale JWT fonctionnelle
- [ ] ✅ Authentification OAuth2 avec Keycloak fonctionnelle
- [ ] ✅ Dual authentication (les 2 méthodes cohabitent)
- [ ] ✅ Tests unitaires avec 80%+ de couverture
- [ ] ✅ Tests de sécurité complets (6 tests minimum requis)
- [ ] ✅ Infrastructure Docker Compose complète
- [ ] ✅ Pipeline Jenkins fonctionnel
- [ ] ✅ SonarQube Quality Gate passé
- [ ] ✅ Configuration CORS sécurisée
- [ ] ✅ Documentation API (Swagger)
- [ ] ✅ Documentation déploiement
- [ ] ✅ Audit logging fonctionnel

---

## 🆘 TROUBLESHOOTING PRÉVENTIF

### Problèmes courants anticipés

#### 1. Keycloak ne démarre pas
```
Symptôme: Container Keycloak crash au démarrage
Cause: Base de données pas prête
Solution: Utiliser health checks dans docker-compose
```

#### 2. Tokens Keycloak non reconnus
```
Symptôme: 401 Unauthorized avec token Keycloak valide
Cause: issuer-uri incorrect ou JWK Set inaccessible
Solution: Vérifier la configuration du issuer-uri et le réseau Docker
```

#### 3. Tests échouent avec OAuth2
```
Symptôme: Tests passent localement mais échouent en CI
Cause: Keycloak non disponible pendant les tests
Solution: Utiliser des mocks pour les tests OAuth2
```

#### 4. Coverage < 80%
```
Symptôme: SonarQube bloque le Quality Gate
Cause: Tests insuffisants
Solution: Identifier les classes non couvertes et ajouter des tests
```

---

## 📊 INDICATEURS DE SUCCÈS

### KPIs du projet
- ✅ **Couverture de tests**: >= 80%
- ✅ **Quality Gate SonarQube**: PASSED
- ✅ **Bugs SonarQube**: 0
- ✅ **Vulnerabilities**: 0
- ✅ **Code Smells**: < 50
- ✅ **Technical Debt**: < 5%
- ✅ **Duplications**: < 3%

### Tests de validation fonctionnelle
1. Un utilisateur peut se connecter avec login/password local
2. Un utilisateur peut se connecter via Keycloak SSO
3. Les rôles Keycloak sont correctement mappés
4. Un utilisateur VIEWER ne peut pas créer de produit
5. Un utilisateur ADMIN peut tout faire
6. Les tokens expirent correctement
7. Le refresh token fonctionne
8. CORS bloque les origines non autorisées
9. Tous les endpoints sont protégés sauf /api/v1/auth/**
10. L'audit log enregistre toutes les actions sensibles

---

## 🎯 CONCLUSION

Ce projet est **ambitieux mais réalisable** en suivant cette roadmap étape par étape.

### Ordre de priorité:
1. **CRITIQUE** 🔴: Apprentissage des concepts (3 semaines)
2. **TRÈS IMPORTANT** 🟠: OAuth2 + Keycloak (3 semaines)
3. **IMPORTANT** 🟡: Tests (2 semaines)
4. **MOYEN** 🟢: CI/CD (2 semaines)
5. **BONUS** ⚪: Documentation avancée

### Conseil final:
**Ne pas sous-estimer la phase d'apprentissage!** Les 3 premières semaines sont cruciales pour comprendre OAuth2 et Keycloak. Prendre le temps de bien maîtriser ces concepts vous fera gagner énormément de temps pendant l'implémentation.

**Bon courage! 💪**

