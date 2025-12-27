# 🎯 OAuth2 - Concepts Fondamentaux

## 📖 Dans ce Chapitre

1. Les 4 Rôles OAuth2
2. Comment ils interagissent
3. Scopes et Permissions
4. Tokens (Access & Refresh)
5. Diagrammes de séquence

---

## 👥 Les 4 Rôles OAuth2

### Vue d'Ensemble

```
┌─────────────────┐
│ Resource Owner  │ ← L'utilisateur (vous)
└────────┬────────┘
         │
    ┌────▼────┐
    │ Client  │ ← L'application tierce
    └────┬────┘
         │
    ┌────▼──────────────┐
    │ Authorization     │ ← Serveur d'autorisation
    │ Server            │
    └────┬──────────────┘
         │
    ┌────▼──────────────┐
    │ Resource Server   │ ← API avec vos données
    └───────────────────┘
```

---

### 1️⃣ Resource Owner (Propriétaire de la Ressource)

**C'est qui ?** L'utilisateur final (VOUS)

**Rôle:**
- Possède les données/ressources
- Autorise ou refuse l'accès
- Donne son consentement

**Exemple concret:**
```
Vous êtes John
Vous possédez:
  - Photos sur Google Photos
  - Contacts sur Gmail
  - Calendrier sur Google Calendar
```

**Actions:**
- ✅ "Oui, j'autorise PrintApp à accéder à mes photos"
- ❌ "Non, je refuse"
- 🔄 "Je révoque l'accès de PrintApp"

---

### 2️⃣ Client (Application Cliente)

**C'est quoi ?** L'application tierce qui veut accéder aux ressources

**Rôle:**
- Demande l'autorisation
- Reçoit le token
- Utilise le token pour accéder aux ressources

**Types de Clients:**

#### a) Confidential Client (Client Confidentiel)
**Peut garder un secret**

```
Exemples:
- Application web avec backend (Node.js, Spring Boot)
- Application serveur
```

**Caractéristiques:**
- ✅ Peut stocker client_secret en sécurité
- ✅ Code s'exécute côté serveur
- ✅ Utilisateur ne voit pas le code

#### b) Public Client (Client Public)
**Ne peut PAS garder un secret**

```
Exemples:
- Application mobile (Android, iOS)
- Single Page Application (React, Angular, Vue)
- Application desktop
```

**Caractéristiques:**
- ❌ Ne peut pas stocker client_secret
- ❌ Code accessible à l'utilisateur
- ❌ Peut être décompilé/inspecté

**Exemple:**
```
PrintApp (Client)
  - Type: Web Application (Confidential)
  - Client ID: printapp-123
  - Client Secret: secret-abc-xyz (gardé sur le serveur)
  - Redirect URI: https://printapp.com/callback
```

---

### 3️⃣ Authorization Server (Serveur d'Autorisation)

**C'est quoi ?** Le serveur qui gère l'authentification et délivre les tokens

**Rôle:**
- Authentifie le Resource Owner
- Affiche l'écran de consentement
- Génère les tokens (access + refresh)
- Valide les tokens
- Révoque les tokens

**Endpoints principaux:**

```
/oauth2/authorize    → Demande d'autorisation
/oauth2/token        → Obtenir un token
/oauth2/revoke       → Révoquer un token
/oauth2/introspect   → Vérifier un token
/.well-known/...     → Configuration OAuth2
```

**Exemple:**
```
Google Authorization Server
  - URL: https://accounts.google.com
  - Gère: Gmail, Drive, Calendar, YouTube...
  - Affiche: "PrintApp veut accéder à vos photos"
  - Génère: Access Token + Refresh Token
```

**Responsabilités:**
1. **Authentification:** Vérifier qui est l'utilisateur
2. **Consentement:** Demander l'autorisation
3. **Token Generation:** Créer les tokens
4. **Token Management:** Valider, rafraîchir, révoquer

---

### 4️⃣ Resource Server (Serveur de Ressources)

**C'est quoi ?** L'API qui héberge les ressources protégées

**Rôle:**
- Héberge les données de l'utilisateur
- Vérifie les tokens
- Retourne les ressources si le token est valide

**Endpoints:**

```
GET /api/photos          → Liste des photos
GET /api/photos/123      → Photo spécifique
POST /api/photos         → Upload photo
DELETE /api/photos/123   → Supprimer photo
```

**Exemple:**
```
Google Photos API (Resource Server)
  - URL: https://photoslibrary.googleapis.com
  - Ressources: Photos, Albums, Métadonnées
  - Vérifie: Access Token dans le header
  - Retourne: Photos si token valide
```

**Validation du Token:**
```
Client → Resource Server: GET /api/photos
                          Header: Authorization: Bearer abc123token

Resource Server → Authorization Server: "Ce token est-il valide?"
Authorization Server → Resource Server: "Oui, valide pour user=john, scope=photos.read"
Resource Server → Client: 200 OK + [photos]
```

---

## 🔄 Interaction entre les Rôles

### Scénario Complet: Imprimer des Photos

```
┌──────────────┐
│ 1. John      │ "Je veux imprimer mes photos"
│ (Resource    │
│  Owner)      │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 2. PrintApp  │ "Redirige John vers Google pour autorisation"
│ (Client)     │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ 3. Google Auth   │ "John, autorises-tu PrintApp à voir tes photos?"
│ (Authorization   │
│  Server)         │
└──────┬───────────┘
       │
       ▼
┌──────────────┐
│ 4. John      │ "Oui, j'autorise"
│ (Resource    │
│  Owner)      │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ 5. Google Auth   │ "Voici un code d'autorisation"
│ (Authorization   │
│  Server)         │
└──────┬───────────┘
       │
       ▼
┌──────────────┐
│ 6. PrintApp  │ "Échange le code contre un token"
│ (Client)     │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ 7. Google Auth   │ "Voici l'Access Token"
│ (Authorization   │
│  Server)         │
└──────┬───────────┘
       │
       ▼
┌──────────────┐
│ 8. PrintApp  │ "Donne-moi les photos (avec token)"
│ (Client)     │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ 9. Google Photos │ "Vérifie le token... OK, voici les photos"
│ (Resource Server)│
└──────────────────┘
```

---

## 🎫 Scopes (Portées)

### C'est quoi ?

Les **scopes** définissent **ce que** le client peut faire avec le token.

### Analogie

```
Badge d'hôtel:
  - Scope: room.302.read    → Entrer dans la chambre 302
  - Scope: gym.access       → Accéder à la gym
  - Scope: pool.access      → Accéder à la piscine
```

### Exemples Réels

#### Google Scopes
```
https://www.googleapis.com/auth/gmail.readonly
  → Lire les emails (pas écrire)

https://www.googleapis.com/auth/gmail.send
  → Envoyer des emails

https://www.googleapis.com/auth/drive.file
  → Accéder aux fichiers créés par l'app

https://www.googleapis.com/auth/drive
  → Accès complet à Drive
```

#### GitHub Scopes
```
repo          → Accès complet aux repos
repo:status   → Accès au statut des commits
public_repo   → Accès aux repos publics uniquement
user:email    → Accès à l'email
```

### Format Standard

```
resource.action

Exemples:
  photos.read
  photos.write
  photos.delete
  profile.read
  contacts.read
```

### Demande de Scopes

```
Client → Authorization Server:
  "Je veux accéder à:
    - photos.read
    - photos.write"

Authorization Server → User:
  "PrintApp demande:
    ✓ Voir vos photos
    ✓ Ajouter des photos
    
    Autoriser?"
```

---

## 🎟️ Tokens

### Access Token (Token d'Accès)

**C'est quoi ?** La clé pour accéder aux ressources

**Caractéristiques:**
- ⏱️ Courte durée de vie (15 min - 1 heure)
- 🔑 Utilisé dans chaque requête API
- 🔒 Contient les scopes autorisés

**Format:**

#### Option 1: JWT (Transparent)
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwic2NvcGUiOiJwaG90b3MucmVhZCIsImV4cCI6MTczNTAwMDAwMH0.signature

Décodé:
{
  "sub": "john",
  "scope": "photos.read photos.write",
  "exp": 1735000000,
  "iat": 1734999000
}
```

#### Option 2: Opaque Token (Référence)
```
2YotnFZFEjr1zCsicMWpAA

Le Resource Server doit appeler l'Authorization Server pour savoir ce que contient ce token
```

**Usage:**
```http
GET /api/photos HTTP/1.1
Host: photos.googleapis.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### Refresh Token (Token de Rafraîchissement)

**C'est quoi ?** Un token pour obtenir un nouveau Access Token

**Pourquoi ?**
- Access Token expire vite (sécurité)
- Refresh Token permet de renouveler sans redemander le mot de passe

**Caractéristiques:**
- ⏱️ Longue durée de vie (jours, semaines, mois)
- 🔐 Stocké de manière sécurisée
- 🔄 Utilisé uniquement avec l'Authorization Server

**Flux:**

```
1. Access Token expire après 15 minutes

Client → Authorization Server:
  POST /oauth2/token
  grant_type=refresh_token
  refresh_token=xyz789...

Authorization Server → Client:
  {
    "access_token": "nouveau_token_abc123",
    "expires_in": 900,
    "refresh_token": "xyz789..." (même ou nouveau)
  }
```

**Comparaison:**

| Aspect | Access Token | Refresh Token |
|--------|--------------|---------------|
| **Durée** | Courte (15min-1h) | Longue (jours-mois) |
| **Usage** | Chaque requête API | Renouveler Access Token |
| **Exposition** | Fréquente | Rare |
| **Stockage** | Mémoire | Sécurisé (DB, encrypted) |
| **Révocable** | Oui | Oui |

---

## 🔐 Lifecycle Complet d'un Token

### Phase 1: Obtention

```
1. User autorise
2. Authorization Server génère:
   - Access Token (expire dans 15 min)
   - Refresh Token (expire dans 30 jours)
3. Client reçoit les deux tokens
```

### Phase 2: Utilisation

```
Client utilise Access Token pour:
  - GET /api/photos
  - POST /api/photos
  - DELETE /api/photos/123
  
Pendant 15 minutes
```

### Phase 3: Expiration

```
Après 15 minutes:
  Client → API: GET /api/photos (avec vieux token)
  API → Client: 401 Unauthorized (token expiré)
```

### Phase 4: Rafraîchissement

```
Client → Auth Server: 
  "Voici mon Refresh Token, donne-moi un nouveau Access Token"

Auth Server → Client:
  "Voici un nouveau Access Token (valide 15 min)"
```

### Phase 5: Révocation

```
User → Auth Server: "Je révoque l'accès de PrintApp"

Auth Server:
  - Invalide le Refresh Token
  - Marque l'Access Token comme révoqué
  
Client → API: GET /api/photos (avec token révoqué)
API → Client: 401 Unauthorized
```

---

## 📊 Tableau Récapitulatif

| Rôle | Responsabilité | Exemple |
|------|----------------|---------|
| **Resource Owner** | Autorise l'accès | John (vous) |
| **Client** | Demande l'accès | PrintApp |
| **Authorization Server** | Délivre les tokens | Google Auth |
| **Resource Server** | Héberge les données | Google Photos API |

---

## 💡 Points Clés à Retenir

1. **4 rôles distincts** avec des responsabilités claires
2. **Scopes** = Ce que le client peut faire
3. **Access Token** = Courte durée, utilisé souvent
4. **Refresh Token** = Longue durée, utilisé rarement
5. **Authorization Server ≠ Resource Server** (peuvent être séparés)

---

## 🎓 Quiz Rapide

**Question 1:** Qui possède les données ?
- ✅ Resource Owner
- ❌ Client
- ❌ Authorization Server

**Question 2:** Quel token est utilisé dans les requêtes API ?
- ✅ Access Token
- ❌ Refresh Token
- ❌ ID Token

**Question 3:** Qui affiche l'écran de consentement ?
- ❌ Client
- ✅ Authorization Server
- ❌ Resource Server

---

## 📚 Prochaine Étape

**Fichier suivant:** `03-OAUTH2-FLOWS.md`

Vous allez apprendre:
- Authorization Code Flow (le plus important)
- Client Credentials Flow
- Implicit Flow (déprécié)
- Password Flow (déprécié)
- Quand utiliser quel flow

**Temps de lecture:** 20 minutes  
**Niveau:** Intermédiaire
