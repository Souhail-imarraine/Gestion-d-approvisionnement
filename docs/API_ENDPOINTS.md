# API Endpoints - Tricol Stock Management

## Base URL
```
http://localhost:8080/api/v1
```

---

## 🔐 Authentication (Public)
**Base:** `/api/auth`

| Méthode | Endpoint | Description | Body |
|---------|----------|-------------|------|
| POST | `/auth/register` | Inscription nouvel utilisateur | `{username, email, password}` |
| POST | `/auth/login` | Connexion | `{username, password}` |
| POST | `/auth/refresh` | Rafraîchir access token | `{refreshToken}` |
| POST | `/auth/logout` | Déconnexion | - |

---

## 👥 Fournisseurs
**Base:** `/api/v1/fournisseurs`

| Méthode | Endpoint | Description | Permission |
|---------|----------|-------------|------------|
| GET | `/fournisseurs` | Liste tous les fournisseurs | READ_FOURNISSEUR |
| GET | `/fournisseurs/{id}` | Détails d'un fournisseur | READ_FOURNISSEUR |
| GET | `/fournisseurs/search?name={name}` | Rechercher par nom | READ_FOURNISSEUR |
| POST | `/fournisseurs` | Créer un fournisseur | CREATE_FOURNISSEUR |
| PUT | `/fournisseurs/{id}` | Modifier un fournisseur | UPDATE_FOURNISSEUR |
| DELETE | `/fournisseurs/{id}` | Supprimer un fournisseur | DELETE_FOURNISSEUR |

**Body POST/PUT:**
```json
{
  "nom": "Fournisseur Test",
  "adresse": "123 Rue Test, Casablanca",
  "telephone": "0522123456",
  "email": "contact@fournisseur.ma"
}
```

---

## 📦 Produits
**Base:** `/api/v1/produits`

| Méthode | Endpoint | Description | Permission |
|---------|----------|-------------|------------|
| GET | `/produits` | Liste tous les produits | READ_PRODUIT |
| GET | `/produits/{id}` | Détails d'un produit | READ_PRODUIT |
| GET | `/produits/{id}/stock` | Stock d'un produit | READ_STOCK |
| GET | `/produits/alertes` | Produits en alerte stock | READ_PRODUIT |
| POST | `/produits` | Créer un produit | CREATE_PRODUIT |
| PUT | `/produits/{id}` | Modifier un produit | UPDATE_PRODUIT |
| DELETE | `/produits/{id}` | Supprimer un produit | DELETE_PRODUIT |

**Body POST/PUT:**
```json
{
  "reference": "PROD-001",
  "designation": "Tissu Coton Premium",
  "unite": "METRE",
  "stockActuel": 0,
  "stockMinimum": 50,
  "stockMaximum": 500
}
```

---

## 🛒 Commandes
**Base:** `/api/v1/commandes`

| Méthode | Endpoint | Description | Permission |
|---------|----------|-------------|------------|
| GET | `/commandes` | Liste toutes les commandes | READ_COMMANDE |
| GET | `/commandes/{id}` | Détails d'une commande | READ_COMMANDE |
| GET | `/commandes/statut/{statut}` | Commandes par statut | READ_COMMANDE |
| GET | `/commandes/fournisseur/{id}` | Commandes d'un fournisseur | READ_COMMANDE |
| POST | `/commandes` | Créer une commande | CREATE_COMMANDE |
| PUT | `/commandes/{id}` | Modifier une commande | UPDATE_COMMANDE |
| DELETE | `/commandes/{id}` | Supprimer une commande | DELETE_COMMANDE |
| PATCH | `/commandes/{id}/statut?statut={statut}` | Changer le statut | VALIDATE_COMMANDE |
| PUT | `/commandes/{id}/reception` | Réceptionner une commande | CREATE_RECEPTION |

**Body POST/PUT:**
```json
{
  "fournisseurId": 1,
  "dateCommande": "2024-01-15",
  "lignes": [
    {
      "produitId": 1,
      "quantite": 100,
      "prixUnitaire": 25.50
    }
  ]
}
```

**Statuts disponibles:** `EN_ATTENTE`, `VALIDEE`, `LIVREE`, `ANNULEE`

---

## 📊 Stock
**Base:** `/api/v1/stock`

| Méthode | Endpoint | Description | Permission |
|---------|----------|-------------|------------|
| GET | `/stock` | État global du stock | READ_STOCK |
| GET | `/stock/produit/{id}` | Détail stock d'un produit | READ_STOCK |
| GET | `/stock/mouvements` | Historique des mouvements | READ_STOCK |
| GET | `/stock/mouvements/produit/{id}` | Mouvements d'un produit | READ_STOCK |
| GET | `/stock/alertes` | Produits en alerte | READ_STOCK |
| GET | `/stock/valorisation` | Valorisation du stock | READ_STOCK |

---

## 📋 Bons de Sortie
**Base:** `/api/v1/bons-sortie`

| Méthode | Endpoint | Description | Permission |
|---------|----------|-------------|------------|
| GET | `/bons-sortie` | Liste tous les bons | READ_BON_SORTIE |
| GET | `/bons-sortie/{id}` | Détails d'un bon | READ_BON_SORTIE |
| GET | `/bons-sortie/atelier/{atelier}` | Bons par atelier | READ_BON_SORTIE |
| POST | `/bons-sortie` | Créer un bon de sortie | CREATE_BON_SORTIE |
| PUT | `/bons-sortie/{id}` | Modifier un bon | UPDATE_BON_SORTIE |
| PUT | `/bons-sortie/{id}/valider` | Valider un bon | UPDATE_BON_SORTIE |
| PUT | `/bons-sortie/{id}/annuler` | Annuler un bon | DELETE_BON_SORTIE |

**Body POST/PUT:**
```json
{
  "dateSortie": "2024-01-20",
  "destination": "Atelier Production",
  "lignes": [
    {
      "lotId": 1,
      "quantite": 20
    }
  ]
}
```

---

## 🔑 Headers Requis

### Pour les endpoints publics (/auth/*)
```
Content-Type: application/json
```

### Pour les endpoints protégés
```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

---

## 📝 Exemples de Requêtes

### 1. Connexion
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
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

### 2. Créer un Fournisseur
```bash
curl -X POST http://localhost:8080/api/v1/fournisseurs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "nom": "Fournisseur ABC",
    "adresse": "Casablanca",
    "telephone": "0522123456",
    "email": "contact@abc.ma"
  }'
```

### 3. Créer une Commande
```bash
curl -X POST http://localhost:8080/api/v1/commandes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "fournisseurId": 1,
    "dateCommande": "2024-01-15",
    "lignes": [
      {
        "produitId": 1,
        "quantite": 100,
        "prixUnitaire": 25.50
      }
    ]
  }'
```

### 4. Réceptionner une Commande
```bash
curl -X PUT http://localhost:8080/api/v1/commandes/1/reception \
  -H "Authorization: Bearer {accessToken}"
```

### 5. Créer un Bon de Sortie
```bash
curl -X POST http://localhost:8080/api/v1/bons-sortie \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "dateSortie": "2024-01-20",
    "destination": "Atelier Production",
    "lignes": [
      {
        "lotId": 1,
        "quantite": 20
      }
    ]
  }'
```

---

## 🎯 Permissions par Rôle

### ADMIN
✅ Toutes les permissions

### RESPONSABLE_ACHATS
✅ Fournisseurs (CRUD)  
✅ Produits (CRUD)  
✅ Commandes (CRUD + Validation)  
✅ Stock (Lecture)

### MAGASINIER
✅ Réceptions (Créer)  
✅ Stock (Lecture)  
✅ Bons de Sortie (CRUD)

### CHEF_ATELIER
✅ Produits (Lecture)  
✅ Stock (Lecture)  
✅ Bons de Sortie (Créer)

---

## ⚠️ Codes de Réponse HTTP

| Code | Description |
|------|-------------|
| 200 | Succès |
| 201 | Créé avec succès |
| 204 | Supprimé avec succès |
| 400 | Requête invalide |
| 401 | Non authentifié |
| 403 | Non autorisé (permissions insuffisantes) |
| 404 | Ressource non trouvée |
| 500 | Erreur serveur |

---

## 🧪 Tester avec Postman

1. Importer la collection: `Tricol_Stock_Management.postman_collection.json`
2. Exécuter "Login Admin" pour obtenir le token
3. Le token est automatiquement sauvegardé dans `{{accessToken}}`
4. Tester tous les endpoints avec authentification automatique
