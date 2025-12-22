# 🧪 Guide de Test des Endpoints - Step by Step

## 📋 Prérequis

- ✅ Application démarrée: `mvn spring-boot:run`
- ✅ MySQL en cours d'exécution
- ✅ Postman installé
- ✅ Collection Postman importée

---

## 🚀 ÉTAPE 1: Préparer la Base de Données (5 min)

### 1.1 Créer l'utilisateur admin

```sql
-- Connectez-vous à MySQL
mysql -u root -p

-- Utilisez la base de données
USE tricol_stock_db;

-- Vérifier si admin existe
SELECT * FROM users WHERE username = 'admin';

-- Si non, créer l'admin
INSERT INTO users (username, email, password, first_name, last_name, enabled)
VALUES ('admin', 'admin@tricol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Tricol', true);

-- Assigner le rôle ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
```

**Résultat attendu:** ✅ Admin créé et activé

---

## 🔐 ÉTAPE 2: Tester l'Authentification (10 min)

### 2.1 Login avec Admin

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/login
Headers: Content-Type: application/json
Body (raw JSON):
{
  "username": "admin",
  "password": "password"
}
```

**Résultat attendu:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "username": "admin",
  "email": "admin@tricol.com"
}
```

✅ **Copier le accessToken** pour les prochaines requêtes

---

### 2.2 Tester Refresh Token

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/refresh
Body:
{
  "refreshToken": "COLLER_LE_REFRESH_TOKEN"
}
```

**Résultat attendu:** ✅ Nouveau accessToken

---

### 2.3 Tester Register

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/register
Body:
{
  "username": "testuser",
  "email": "test@tricol.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

**Résultat attendu:** ✅ User créé (enabled=false)

---

## 👥 ÉTAPE 3: Tester Fournisseurs (15 min)

### 3.1 Créer un Fournisseur

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer VOTRE_ACCESS_TOKEN
Body:
{
  "nom": "Fournisseur ABC",
  "adresse": "123 Rue Test, Casablanca",
  "telephone": "0522123456",
  "email": "contact@abc.ma"
}
```

**Résultat attendu:**
```json
{
  "id": 1,
  "nom": "Fournisseur ABC",
  "adresse": "123 Rue Test, Casablanca",
  "telephone": "0522123456",
  "email": "contact@abc.ma"
}
```

✅ **Noter l'ID du fournisseur**

---

### 3.2 Lister les Fournisseurs

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs
Headers: Authorization: Bearer VOTRE_ACCESS_TOKEN
```

**Résultat attendu:** ✅ Liste avec le fournisseur créé

---

### 3.3 Modifier le Fournisseur

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs/1
Headers: Authorization: Bearer VOTRE_ACCESS_TOKEN
Body:
{
  "nom": "Fournisseur ABC Updated",
  "adresse": "456 Avenue Nouvelle, Rabat",
  "telephone": "0537654321",
  "email": "nouveau@abc.ma"
}
```

**Résultat attendu:** ✅ Fournisseur modifié

---

### 3.4 Rechercher un Fournisseur

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs/search?name=ABC
Headers: Authorization: Bearer VOTRE_ACCESS_TOKEN
```

**Résultat attendu:** ✅ Fournisseur trouvé

---

## 📦 ÉTAPE 4: Tester Produits (15 min)

### 4.1 Créer un Produit

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/produits
Headers: Authorization: Bearer VOTRE_ACCESS_TOKEN
Body:
{
  "reference": "PROD-001",
  "designation": "Tissu Coton Premium",
  "unite": "METRE",
  "stockActuel": 0,
  "stockMinimum": 50,
  "stockMaximum": 500
}
```

**Résultat attendu:** ✅ Produit créé avec ID

✅ **Noter l'ID du produit**

---

### 4.2 Créer un Deuxième Produit

**Postman:**
```
Body:
{
  "reference": "PROD-002",
  "designation": "Tissu Lin",
  "unite": "METRE",
  "stockActuel": 0,
  "stockMinimum": 30,
  "stockMaximum": 300
}
```

---

### 4.3 Lister les Produits

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/produits
```

**Résultat attendu:** ✅ 2 produits

---

### 4.4 Voir les Alertes Stock

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/produits/alertes
```

**Résultat attendu:** ✅ 2 produits en alerte (stock=0 < stockMinimum)

---

## 🛒 ÉTAPE 5: Tester Commandes (20 min)

### 5.1 Créer une Commande

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/commandes
Body:
{
  "fournisseurId": 1,
  "dateCommande": "2024-12-21",
  "lignes": [
    {
      "produitId": 1,
      "quantite": 100,
      "prixUnitaire": 25.50
    },
    {
      "produitId": 2,
      "quantite": 50,
      "prixUnitaire": 35.00
    }
  ]
}
```

**Résultat attendu:**
```json
{
  "id": 1,
  "numero": "CMD-20241221-0001",
  "statut": "EN_ATTENTE",
  "montantTotal": 4300.00
}
```

✅ **Noter l'ID de la commande**

---

### 5.2 Valider la Commande

**Postman:**
```
Method: PATCH
URL: http://localhost:8081/tricol-stock/api/v1/commandes/1/statut?statut=VALIDEE
```

**Résultat attendu:** ✅ Statut = VALIDEE

---

### 5.3 Lister les Commandes

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/commandes
```

**Résultat attendu:** ✅ Commande avec statut VALIDEE

---

### 5.4 Filtrer par Statut

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/commandes/statut/VALIDEE
```

---

## 📥 ÉTAPE 6: Tester Réception (10 min)

### 6.1 Réceptionner la Commande

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/commandes/1/reception
```

**Résultat attendu:**
```json
{
  "id": 1,
  "statut": "LIVREE"
}
```

**Ce qui se passe:**
- ✅ Création de 2 lots de stock
- ✅ Création de 2 mouvements d'entrée
- ✅ Mise à jour du stock des produits

---

### 6.2 Vérifier le Stock

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/stock
```

**Résultat attendu:**
```json
[
  {
    "produitId": 1,
    "designation": "Tissu Coton Premium",
    "stockActuel": 100,
    "stockMinimum": 50
  },
  {
    "produitId": 2,
    "designation": "Tissu Lin",
    "stockActuel": 50,
    "stockMinimum": 30
  }
]
```

---

## 📊 ÉTAPE 7: Tester Stock (10 min)

### 7.1 Voir l'Historique des Mouvements

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/stock/mouvements
```

**Résultat attendu:** ✅ 2 mouvements d'ENTREE

---

### 7.2 Voir la Valorisation

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/stock/valorisation
```

**Résultat attendu:**
```json
{
  "valorisationTotale": 4300.00,
  "nombreProduits": 2
}
```

---

### 7.3 Détail Stock d'un Produit

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/stock/produit/1
```

**Résultat attendu:** ✅ Détails avec lots

---

## 📋 ÉTAPE 8: Tester Bons de Sortie (15 min)

### 8.1 Créer un Bon de Sortie

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/bons-sortie
Body:
{
  "dateSortie": "2024-12-21",
  "destination": "Atelier Production",
  "lignes": [
    {
      "lotId": 1,
      "quantite": 20
    },
    {
      "lotId": 2,
      "quantite": 10
    }
  ]
}
```

**Résultat attendu:**
```json
{
  "id": 1,
  "numero": "BS-20241221-0001",
  "destination": "Atelier Production"
}
```

---

### 8.2 Vérifier le Stock Après Sortie

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/stock
```

**Résultat attendu:**
```json
[
  {
    "produitId": 1,
    "stockActuel": 80  // 100 - 20
  },
  {
    "produitId": 2,
    "stockActuel": 40  // 50 - 10
  }
]
```

---

### 8.3 Valider le Bon de Sortie

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/bons-sortie/1/valider
```

---

### 8.4 Lister les Bons de Sortie

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/bons-sortie
```

---

## 🔒 ÉTAPE 9: Tester les Permissions (20 min)

### 9.1 Créer un Utilisateur MAGASINIER

**SQL:**
```sql
INSERT INTO users (username, email, password, enabled)
VALUES ('magasinier1', 'mag@tricol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'magasinier1' AND r.name = 'MAGASINIER';
```

---

### 9.2 Login avec MAGASINIER

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/login
Body:
{
  "username": "magasinier1",
  "password": "password"
}
```

✅ **Copier le nouveau accessToken**

---

### 9.3 Test: MAGASINIER PEUT Consulter Fournisseurs

**Postman:**
```
Method: GET
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs
Headers: Authorization: Bearer TOKEN_MAGASINIER
```

**Résultat attendu:** ✅ 200 OK - Liste des fournisseurs

---

### 9.4 Test: MAGASINIER NE PEUT PAS Créer Fournisseur

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs
Headers: Authorization: Bearer TOKEN_MAGASINIER
Body: {...}
```

**Résultat attendu:** ❌ 403 Forbidden

---

### 9.5 Test: MAGASINIER PEUT Créer Bon de Sortie

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/bons-sortie
Headers: Authorization: Bearer TOKEN_MAGASINIER
Body: {...}
```

**Résultat attendu:** ✅ 201 Created

---

### 9.6 Test: MAGASINIER PEUT Réceptionner

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/commandes/2/reception
Headers: Authorization: Bearer TOKEN_MAGASINIER
```

**Résultat attendu:** ✅ 200 OK

---

## ✅ ÉTAPE 10: Checklist Finale

### Authentification
- [ ] Login admin fonctionne
- [ ] Refresh token fonctionne
- [ ] Register fonctionne
- [ ] Logout fonctionne

### Fournisseurs
- [ ] Créer fournisseur (ADMIN) ✅
- [ ] Lister fournisseurs ✅
- [ ] Modifier fournisseur ✅
- [ ] Rechercher fournisseur ✅
- [ ] Créer fournisseur (MAGASINIER) ❌ 403

### Produits
- [ ] Créer produit ✅
- [ ] Lister produits ✅
- [ ] Voir alertes ✅
- [ ] Modifier produit ✅

### Commandes
- [ ] Créer commande ✅
- [ ] Valider commande ✅
- [ ] Réceptionner commande ✅
- [ ] Lister commandes ✅

### Stock
- [ ] Voir stock global ✅
- [ ] Voir mouvements ✅
- [ ] Voir valorisation ✅
- [ ] Stock mis à jour après réception ✅

### Bons de Sortie
- [ ] Créer bon de sortie ✅
- [ ] Valider bon de sortie ✅
- [ ] Stock mis à jour après sortie ✅
- [ ] Lister bons de sortie ✅

### Permissions
- [ ] MAGASINIER peut consulter ✅
- [ ] MAGASINIER ne peut pas créer fournisseur ❌ 403
- [ ] MAGASINIER peut créer bon de sortie ✅
- [ ] MAGASINIER peut réceptionner ✅

---

## 🎉 RÉSULTAT FINAL

Si toutes les cases sont cochées:

```
╔═══════════════════════════════════════════╗
║  ✅ TOUS LES ENDPOINTS FONCTIONNENT      ║
║                                           ║
║  ✓ Authentification: OK                  ║
║  ✓ CRUD Fournisseurs: OK                 ║
║  ✓ CRUD Produits: OK                     ║
║  ✓ CRUD Commandes: OK                    ║
║  ✓ Réception: OK                         ║
║  ✓ Stock: OK                             ║
║  ✓ Bons de Sortie: OK                    ║
║  ✓ Permissions: OK                       ║
║                                           ║
║  L'APPLICATION EST PRÊTE! 🚀             ║
╚═══════════════════════════════════════════╝
```

**Temps total estimé:** 2 heures
