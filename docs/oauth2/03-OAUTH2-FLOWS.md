# 🔄 OAuth2 - Les Flows (Grant Types)

## 📖 Dans ce Chapitre

1. Vue d'ensemble des 4 flows
2. Authorization Code Flow (LE PLUS IMPORTANT)
3. Client Credentials Flow
4. Implicit Flow (DÉPRÉCIÉ)
5. Password Flow (DÉPRÉCIÉ)
6. PKCE (Protection moderne)
7. Quand utiliser quel flow

---

## 🎯 Les 4 Grant Types OAuth2

### Vue d'Ensemble

| Flow | Usage | Sécurité | Status |
|------|-------|----------|--------|
| **Authorization Code** | Web Apps, Mobile Apps | ⭐⭐⭐⭐⭐ | ✅ Recommandé |
| **Client Credentials** | Server-to-Server | ⭐⭐⭐⭐ | ✅ Recommandé |
| **Implicit** | SPA (ancien) | ⭐⭐ | ❌ Déprécié |
| **Password** | Apps de confiance | ⭐ | ❌ Déprécié |

---

## 1️⃣ Authorization Code Flow

### 🎯 Le Plus Important et Recommandé

**Utilisé pour:**
- Applications web avec backend
- Applications mobiles (avec PKCE)
- Applications qui peuvent garder un secret

**Principe:**
L'utilisateur autorise l'app, reçoit un **code**, puis l'app échange ce code contre un **token**.

---

### 📊 Diagramme de Séquence Complet

```
┌──────┐          ┌────────┐          ┌──────────┐          ┌──────────┐
│ User │          │ Client │          │ Auth     │          │Resource  │
│      │          │  App   │          │ Server   │          │ Server   │
└──┬───┘          └───┬────┘          └────┬─────┘          └────┬─────┘
   │                  │                     │                     │
   │ 1. Clic "Login"  │                     │                     │
   ├─────────────────>│                     │                     │
   │                  │                     │                     │
   │                  │ 2. Redirect to Auth │                     │
   │                  ├────────────────────>│                     │
   │                  │ /authorize?         │                     │
   │                  │   client_id=abc     │                     │
   │                  │   redirect_uri=...  │                     │
   │                  │   scope=photos.read │                     │
   │                  │                     │                     │
   │                  │ 3. Login Page       │                     │
   │<─────────────────┴─────────────────────┤                     │
   │                                        │                     │
   │ 4. Enter credentials                   │                     │
   ├───────────────────────────────────────>│                     │
   │                                        │                     │
   │ 5. Consent Screen                      │                     │
   │    "Allow access to photos?"           │                     │
   │<───────────────────────────────────────┤                     │
   │                                        │                     │
   │ 6. Click "Allow"                       │                     │
   ├───────────────────────────────────────>│                     │
   │                                        │                     │
   │ 7. Redirect with CODE                  │                     │
   │<───────────────────────────────────────┤                     │
   │ callback?code=xyz123                   │                     │
   │                                        │                     │
   │                  │ 8. Receive code     │                     │
   │                  │<────────────────────┤                     │
   │                  │                     │                     │
   │                  │ 9. Exchange code    │                     │
   │                  │    for token        │                     │
   │                  ├────────────────────>│                     │
   │                  │ POST /token         │                     │
   │                  │   code=xyz123       │                     │
   │                  │   client_id=abc     │                     │
   │                  │   client_secret=... │                     │
   │                  │                     │                     │
   │                  │ 10. Access Token    │                     │
   │                  │<────────────────────┤                     │
   │                  │ {                   │                     │
   │                  │   access_token,     │                     │
   │                  │   refresh_token     │                     │
   │                  │ }                   │                     │
   │                  │                     │                     │
   │                  │ 11. API Request     │                     │
   │                  ├─────────────────────┼────────────────────>│
   │                  │ GET /api/photos     │                     │
   │                  │ Authorization:      │                     │
   │                  │   Bearer token      │                     │
   │                  │                     │                     │
   │                  │ 12. Photos          │                     │
   │                  │<────────────────────┼─────────────────────┤
   │                  │                     │                     │
   │ 13. Display      │                     │                     │
   │<─────────────────┤                     │                     │
```

---

### 🔍 Étape par Étape

#### Étape 1-2: Redirection vers Authorization Server

**Client → User:**
```
User clique sur "Se connecter avec Google"
```

**Client → Authorization Server:**
```http
GET /oauth2/authorize?
  response_type=code&
  client_id=printapp-123&
  redirect_uri=https://printapp.com/callback&
  scope=photos.read photos.write&
  state=random-string-xyz
```

**Paramètres:**
- `response_type=code` → Je veux un code d'autorisation
- `client_id` → Identifiant de l'app
- `redirect_uri` → Où renvoyer l'utilisateur
- `scope` → Ce que l'app veut faire
- `state` → Protection CSRF (random string)

---

#### Étape 3-4: Authentification

**Authorization Server → User:**
```
Page de login Google:
  Email: _________
  Password: _________
  [Se connecter]
```

User entre ses credentials et se connecte.

---

#### Étape 5-6: Consentement

**Authorization Server → User:**
```
┌─────────────────────────────────────┐
│ PrintApp veut accéder à:            │
│                                     │
│ ✓ Voir vos photos                   │
│ ✓ Ajouter des photos                │
│                                     │
│ [Refuser]  [Autoriser]              │
└─────────────────────────────────────┘
```

User clique sur "Autoriser".

---

#### Étape 7-8: Redirection avec Code

**Authorization Server → Client:**
```http
HTTP/1.1 302 Found
Location: https://printapp.com/callback?
  code=AUTH_CODE_xyz123&
  state=random-string-xyz
```

**Client reçoit:**
- `code=AUTH_CODE_xyz123` → Code d'autorisation (valide 10 minutes)
- `state=random-string-xyz` → Vérifie que c'est la même requête

---

#### Étape 9-10: Échange Code contre Token

**Client → Authorization Server:**
```http
POST /oauth2/token HTTP/1.1
Host: accounts.google.com
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=AUTH_CODE_xyz123&
redirect_uri=https://printapp.com/callback&
client_id=printapp-123&
client_secret=SECRET_abc_xyz
```

**Authorization Server → Client:**
```json
{
  "access_token": "ya29.a0AfH6SMBx...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "1//0gKN8...",
  "scope": "photos.read photos.write"
}
```

---

#### Étape 11-12: Utilisation du Token

**Client → Resource Server:**
```http
GET /api/photos HTTP/1.1
Host: photoslibrary.googleapis.com
Authorization: Bearer ya29.a0AfH6SMBx...
```

**Resource Server → Client:**
```json
{
  "photos": [
    {"id": 1, "url": "https://..."},
    {"id": 2, "url": "https://..."}
  ]
}
```

---

### 🔐 Pourquoi c'est Sécurisé?

1. **Code temporaire** (10 min) au lieu du token
2. **Client Secret** requis pour échanger le code
3. **Code utilisé une seule fois**
4. **State parameter** protège contre CSRF
5. **Redirect URI** doit être pré-enregistré

---

### 💻 Code Spring Boot - Client

```java
@Configuration
public class OAuth2ClientConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback")
                )
            );
        return http.build();
    }
}
```

**application.yml:**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: printapp-123
            client-secret: SECRET_abc_xyz
            scope:
              - photos.read
              - photos.write
            redirect-uri: "{baseUrl}/oauth2/callback"
            authorization-grant-type: authorization_code
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
```

---

## 2️⃣ Client Credentials Flow

### 🎯 Server-to-Server

**Utilisé pour:**
- Communication entre services (microservices)
- Batch jobs
- CLI tools
- Pas d'utilisateur impliqué

**Principe:**
Le client s'authentifie directement avec ses credentials et reçoit un token.

---

### 📊 Diagramme de Séquence

```
┌────────┐          ┌──────────┐          ┌──────────┐
│ Client │          │ Auth     │          │Resource  │
│Service │          │ Server   │          │ Server   │
└───┬────┘          └────┬─────┘          └────┬─────┘
    │                    │                     │
    │ 1. Request Token   │                     │
    ├───────────────────>│                     │
    │ POST /token        │                     │
    │   grant_type=      │                     │
    │     client_creds   │                     │
    │   client_id=...    │                     │
    │   client_secret=...│                     │
    │                    │                     │
    │ 2. Access Token    │                     │
    │<───────────────────┤                     │
    │ {                  │                     │
    │   access_token,    │                     │
    │   expires_in       │                     │
    │ }                  │                     │
    │                    │                     │
    │ 3. API Request     │                     │
    ├────────────────────┼────────────────────>│
    │ GET /api/data      │                     │
    │ Authorization:     │                     │
    │   Bearer token     │                     │
    │                    │                     │
    │ 4. Data            │                     │
    │<───────────────────┼─────────────────────┤
```

---

### 🔍 Étape par Étape

#### Étape 1: Demande de Token

**Client → Authorization Server:**
```http
POST /oauth2/token HTTP/1.1
Host: auth.example.com
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&
client_id=service-a-123&
client_secret=SECRET_service_a&
scope=api.read api.write
```

#### Étape 2: Réception du Token

**Authorization Server → Client:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "api.read api.write"
}
```

**Note:** Pas de Refresh Token (le client peut redemander un token)

---

### 💻 Code Spring Boot

**Client:**
```java
@Service
public class ApiClient {
    
    @Value("${oauth2.token-uri}")
    private String tokenUri;
    
    @Value("${oauth2.client-id}")
    private String clientId;
    
    @Value("${oauth2.client-secret}")
    private String clientSecret;
    
    private final RestTemplate restTemplate;
    
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", "api.read api.write");
        
        HttpEntity<MultiValueMap<String, String>> request = 
            new HttpEntity<>(body, headers);
        
        TokenResponse response = restTemplate.postForObject(
            tokenUri, 
            request, 
            TokenResponse.class
        );
        
        return response.getAccessToken();
    }
    
    public String callApi(String endpoint) {
        String token = getAccessToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
            endpoint,
            HttpMethod.GET,
            request,
            String.class
        ).getBody();
    }
}
```

---

### ⚠️ Cas d'Usage

**✅ Bon:**
- Service A appelle Service B
- Cron job qui synchronise des données
- CLI tool pour admin

**❌ Mauvais:**
- Application web avec utilisateurs
- Application mobile
- Tout ce qui implique un utilisateur final

---

## 3️⃣ Implicit Flow (DÉPRÉCIÉ)

### ⚠️ NE PLUS UTILISER

**Pourquoi déprécié?**
- Token exposé dans l'URL
- Pas de client authentication
- Pas de refresh token
- Vulnérable aux attaques

**Remplacé par:** Authorization Code Flow + PKCE

---

### 📊 Diagramme (pour comprendre)

```
User → Client: Clic "Login"
Client → Auth Server: /authorize?response_type=token
Auth Server → User: Login + Consent
User → Auth Server: Approve
Auth Server → Client: Redirect avec #access_token=xyz (dans URL!)
Client: Utilise le token
```

**Problème:** Le token est dans l'URL du navigateur!

```
https://app.com/callback#access_token=SECRET_TOKEN_123&expires_in=3600
```

❌ Visible dans l'historique  
❌ Peut être intercepté  
❌ Pas sécurisé

---

## 4️⃣ Password Flow (DÉPRÉCIÉ)

### ⚠️ NE PLUS UTILISER

**Principe:**
L'app demande directement username + password à l'utilisateur.

```
User → Client: username + password
Client → Auth Server: Envoie username + password
Auth Server → Client: Access Token
```

**Pourquoi déprécié?**
- L'app voit le mot de passe de l'utilisateur
- Contre le principe OAuth2
- Pas de consentement explicite
- Risque de phishing

**Utilisé uniquement pour:** Apps de confiance absolue (ex: app officielle)

---

## 5️⃣ PKCE (Proof Key for Code Exchange)

### 🛡️ Protection Moderne

**C'est quoi?**
Une extension de Authorization Code Flow pour les **Public Clients** (mobiles, SPAs).

**Problème résolu:**
Protège contre l'interception du code d'autorisation.

---

### 📊 Diagramme PKCE

```
Client génère:
  code_verifier = random_string_43_chars
  code_challenge = SHA256(code_verifier)

Client → Auth Server: /authorize?
  response_type=code&
  code_challenge=SHA256_hash&
  code_challenge_method=S256

Auth Server → Client: code=xyz123

Client → Auth Server: /token?
  code=xyz123&
  code_verifier=random_string_43_chars

Auth Server:
  - Calcule SHA256(code_verifier)
  - Compare avec code_challenge
  - Si match → Donne le token
```

---

### 💻 Code PKCE

```java
// 1. Générer code_verifier
String codeVerifier = generateCodeVerifier();

// 2. Générer code_challenge
String codeChallenge = generateCodeChallenge(codeVerifier);

// 3. Demande d'autorisation
String authUrl = "https://auth.example.com/authorize?" +
    "response_type=code&" +
    "client_id=mobile-app&" +
    "redirect_uri=myapp://callback&" +
    "code_challenge=" + codeChallenge + "&" +
    "code_challenge_method=S256";

// 4. Échange code contre token
Map<String, String> params = new HashMap<>();
params.put("grant_type", "authorization_code");
params.put("code", authorizationCode);
params.put("redirect_uri", "myapp://callback");
params.put("client_id", "mobile-app");
params.put("code_verifier", codeVerifier); // Prouve que c'est le même client

// Helper methods
private String generateCodeVerifier() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(bytes);
}

private String generateCodeChallenge(String verifier) {
    byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest(bytes);
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(digest);
}
```

---

## 📊 Tableau Comparatif

| Flow | User Involved | Client Secret | Refresh Token | Usage |
|------|---------------|---------------|---------------|-------|
| **Authorization Code** | ✅ Oui | ✅ Oui | ✅ Oui | Web Apps |
| **Authorization Code + PKCE** | ✅ Oui | ❌ Non | ✅ Oui | Mobile, SPA |
| **Client Credentials** | ❌ Non | ✅ Oui | ❌ Non | Server-to-Server |
| **Implicit** | ✅ Oui | ❌ Non | ❌ Non | ❌ Déprécié |
| **Password** | ✅ Oui | ✅ Oui | ✅ Oui | ❌ Déprécié |

---

## 🎯 Quand Utiliser Quel Flow?

### Application Web (avec backend)
✅ **Authorization Code Flow**
```
Frontend → Backend → Auth Server
Backend garde le client_secret
```

### Application Mobile (iOS, Android)
✅ **Authorization Code Flow + PKCE**
```
Pas de client_secret
PKCE protège le code
```

### Single Page Application (React, Vue, Angular)
✅ **Authorization Code Flow + PKCE**
```
Pas de backend
PKCE obligatoire
```

### Microservices (Service-to-Service)
✅ **Client Credentials Flow**
```
Pas d'utilisateur
Service A → Service B
```

### Application de confiance (votre propre app)
🟡 **Password Flow** (si vraiment nécessaire)
```
Seulement si vous contrôlez 100% l'app
Sinon, utilisez Authorization Code
```

---

## 💡 Points Clés à Retenir

1. **Authorization Code = Le plus sécurisé et recommandé**
2. **PKCE = Obligatoire pour mobiles et SPAs**
3. **Client Credentials = Server-to-Server uniquement**
4. **Implicit & Password = Dépréciés, ne plus utiliser**
5. **Toujours utiliser HTTPS en production**

---

## 🎓 Quiz Rapide

**Q1:** Quel flow pour une app React sans backend?
- ✅ Authorization Code + PKCE
- ❌ Implicit
- ❌ Client Credentials

**Q2:** Quel flow pour un cron job?
- ❌ Authorization Code
- ✅ Client Credentials
- ❌ Password

**Q3:** PKCE protège contre quoi?
- ✅ Interception du code d'autorisation
- ❌ Vol du token
- ❌ Attaque CSRF

---

## 📚 Prochaine Étape

**Fichier suivant:** `04-SPRING-SECURITY-OAUTH2.md`

Vous allez apprendre:
- Architecture Spring Security OAuth2
- Implémenter un Authorization Server
- Implémenter un Resource Server
- Configuration complète

**Temps de lecture:** 30 minutes  
**Niveau:** Intermédiaire-Avancé
