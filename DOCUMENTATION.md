# Documentation - Système de Gestion d'Approvisionnement et Stock

## 📋 Vue d'ensemble du projet

Ce projet est un **système de gestion d'approvisionnement et de stock** développé avec Spring Boot. Il permet de gérer:
- Les fournisseurs
- Les produits
- Les commandes d'approvisionnement
- Les réceptions de marchandises
- Les sorties de stock (bons de sortie)
- Le suivi des stocks avec méthode FIFO (First In, First Out)
- Les mouvements de stock
- Les alertes de stock

---

## 🏗️ Architecture du projet

### Technologies utilisées
- **Backend:** Spring Boot 3.x
- **Base de données:** MySQL
- **ORM:** Hibernate/JPA
- **Migration:** Liquibase
- **Mapping:** MapStruct
- **Build:** Maven

### Structure des packages
```
com.tricol.stock
├── controller/       # Endpoints REST API
├── service/          # Logique métier
├── repository/       # Accès aux données
├── entity/           # Entités JPA
├── dto/              # Data Transfer Objects
├── mapper/           # Conversion Entity ↔ DTO
├── enums/            # Énumérations
└── exception/        # Gestion des exceptions
```

---

## 🔄 Flux de travail principal

### 1. Gestion des Fournisseurs
Créer et gérer les fournisseurs qui fourniront les produits.

### 2. Gestion des Produits
Créer le catalogue de produits avec leurs caractéristiques (prix, seuil d'alerte, etc.).

### 3. Cycle de Commande
```
Créer Commande (EN_ATTENTE)
    ↓
Valider Commande (VALIDEE)
    ↓
Réceptionner Commande (LIVREE)
    ↓
Création automatique de:
    - Lots de stock (FIFO)
    - Mouvements d'ENTREE
    - Mise à jour du stock
```

### 4. Cycle de Sortie
```
Créer Bon de Sortie (BROUILLON)
    ↓
Valider Bon de Sortie (VALIDE)
    ↓
Consommation automatique:
    - Lots FIFO (plus ancien d'abord)
    - Mouvements de SORTIE
    - Réduction du stock
```

---

## 📚 Guide des Tests API

### Variables d'environnement Postman
```
baseURL = http://localhost:8080
contextPath = /tricol-stock
getURL = {{baseURL}}{{contextPath}}
```

---

## 1️⃣ FOURNISSEURS

### Créer un fournisseur
```http
POST {{getURL}}/api/v1/fournisseurs
Content-Type: application/json

{
  "raisonSociale": "TechnoPlus SARL",
  "adresse": "123 Rue de la Technologie",
  "ville": "Casablanca",
  "personneContact": "Ahmed Bennani",
  "email": "contact@technoplus.ma",
  "telephone": "0522123456",
  "ice": "002345678901234"
}
```

### Lister tous les fournisseurs
```http
GET {{getURL}}/api/v1/fournisseurs
```

### Obtenir un fournisseur par ID
```http
GET {{getURL}}/api/v1/fournisseurs/1
```

### Modifier un fournisseur
```http
PUT {{getURL}}/api/v1/fournisseurs/1
Content-Type: application/json

{
  "raisonSociale": "TechnoPlus SARL",
  "adresse": "456 Avenue Nouvelle",
  "ville": "Rabat",
  "personneContact": "Ahmed Bennani",
  "email": "contact@technoplus.ma",
  "telephone": "0522123456",
  "ice": "002345678901234"
}
```

### Supprimer un fournisseur
```http
DELETE {{getURL}}/api/v1/fournisseurs/1
```

### Rechercher par nom
```http
GET {{getURL}}/api/v1/fournisseurs/search?name=Techno
```

---

## 2️⃣ PRODUITS

### Créer un produit
```http
POST {{getURL}}/api/v1/produits
Content-Type: application/json

{
  "reference": "SOURIS-001",
  "nom": "Souris Sans Fil Logitech",
  "description": "Souris optique sans fil 2.4GHz",
  "prixUnitaire": 150.00,
  "categorie": "INFORMATIQUE",
  "stockActuel": 50,
  "pointCommande": 10,
  "uniteMesure": "UNITE"
}
```

### Lister tous les produits
```http
GET {{getURL}}/api/v1/produits
```

### Obtenir un produit par ID
```http
GET {{getURL}}/api/v1/produits/5
```

### Modifier un produit
```http
PUT {{getURL}}/api/v1/produits/5
Content-Type: application/json

{
  "reference": "SOURIS-001",
  "nom": "Souris Sans Fil Logitech MX",
  "description": "Souris ergonomique sans fil",
  "prixUnitaire": 180.00,
  "categorie": "INFORMATIQUE",
  "stockActuel": 50,
  "pointCommande": 15,
  "uniteMesure": "UNITE"
}
```

### Supprimer un produit
```http
DELETE {{getURL}}/api/v1/produits/5
```

### Produits en alerte
```http
GET {{getURL}}/api/v1/produits/alertes
```

### Consulter le stock d'un produit
```http
GET {{getURL}}/api/v1/produits/5/stock
```

**Réponse:**
```json
{
  "produitId": 5,
  "produitNom": "Souris Sans Fil Logitech",
  "stockActuel": 50,
  "pointCommande": 10,
  "uniteMesure": "UNITE",
  "enAlerte": false
}
```

---

## 3️⃣ COMMANDES

### Créer une commande
```http
POST {{getURL}}/api/v1/commandes
Content-Type: application/json

{
  "dateCommande": "2025-01-15",
  "dateLivraisonPrevue": "2025-01-20",
  "fournisseurId": 1,
  "lignes": [
    {
      "produitId": 5,
      "quantite": 100,
      "prixUnitaire": 150.00
    },
    {
      "produitId": 7,
      "quantite": 50,
      "prixUnitaire": 6500.00
    }
  ]
}
```

**Note:** Le `numero` et `statut` sont générés automatiquement.

### Lister toutes les commandes
```http
GET {{getURL}}/api/v1/commandes
```

### Obtenir une commande par ID
```http
GET {{getURL}}/api/v1/commandes/11
```

### Modifier une commande
```http
PUT {{getURL}}/api/v1/commandes/11
Content-Type: application/json

{
  "dateCommande": "2025-01-15",
  "dateLivraisonPrevue": "2025-01-25",
  "fournisseurId": 1,
  "lignes": [
    {
      "produitId": 5,
      "quantite": 150,
      "prixUnitaire": 150.00
    }
  ]
}
```

### Supprimer une commande
```http
DELETE {{getURL}}/api/v1/commandes/11
```

### Changer le statut d'une commande
```http
PATCH {{getURL}}/api/v1/commandes/11/statut?statut=VALIDEE
```

**Statuts disponibles:**
- `EN_ATTENTE` (par défaut)
- `VALIDEE`
- `LIVREE`
- `ANNULEE`

### Filtrer par statut
```http
GET {{getURL}}/api/v1/commandes/statut/VALIDEE
```

### Filtrer par fournisseur
```http
GET {{getURL}}/api/v1/commandes/fournisseur/1
```

### Réceptionner une commande
```http
PUT {{getURL}}/api/v1/commandes/11/reception
```

**Prérequis:** La commande doit être au statut `VALIDEE`

**Effets:**
- Crée des lots de stock (FIFO)
- Crée des mouvements d'ENTREE
- Augmente le stock des produits
- Change le statut à `LIVREE`

---

## 4️⃣ BONS DE SORTIE

### Créer un bon de sortie
```http
POST {{getURL}}/api/v1/bons-sortie
Content-Type: application/json

{
  "atelier": "ATELIER_A",
  "commentaire": "Sortie pour production",
  "lignes": [
    {
      "produitId": 5,
      "quantite": 10
    },
    {
      "produitId": 7,
      "quantite": 5
    }
  ]
}
```

**Note:** Le `numero` et `statut` (BROUILLON) sont générés automatiquement.

### Lister tous les bons
```http
GET {{getURL}}/api/v1/bons-sortie
```

### Obtenir un bon par ID
```http
GET {{getURL}}/api/v1/bons-sortie/2
```

### Modifier un bon (BROUILLON uniquement)
```http
PUT {{getURL}}/api/v1/bons-sortie/2
Content-Type: application/json

{
  "atelier": "ATELIER_B",
  "commentaire": "Modification",
  "lignes": [
    {
      "produitId": 5,
      "quantite": 15
    }
  ]
}
```

### Valider un bon de sortie
```http
PUT {{getURL}}/api/v1/bons-sortie/2/valider
```

**Prérequis:** 
- Le bon doit être au statut `BROUILLON`
- Des lots doivent exister pour les produits

**Effets:**
- Consomme les lots FIFO (plus anciens d'abord)
- Crée des mouvements de SORTIE
- Réduit le stock des produits
- Change le statut à `VALIDE`

### Annuler un bon (BROUILLON uniquement)
```http
PUT {{getURL}}/api/v1/bons-sortie/2/annuler
```

### Filtrer par atelier
```http
GET {{getURL}}/api/v1/bons-sortie/atelier/ATELIER_A
```

---

## 5️⃣ GESTION DU STOCK

### État global du stock
```http
GET {{getURL}}/api/v1/stock
```

**Réponse:**
```json
[
  {
    "produitId": 5,
    "produitNom": "Souris Sans Fil Logitech",
    "produitReference": "SOURIS-001",
    "stockActuel": 140,
    "pointCommande": 10,
    "enAlerte": false,
    "valeurStock": 21000.00
  }
]
```

### Détail du stock par produit (avec lots FIFO)
```http
GET {{getURL}}/api/v1/stock/produit/5
```

**Réponse:**
```json
{
  "produitId": 5,
  "produitNom": "Souris Sans Fil Logitech",
  "produitReference": "SOURIS-001",
  "stockActuel": 140,
  "pointCommande": 10,
  "valeurTotale": 21000.00,
  "lots": [
    {
      "id": 1,
      "numeroLot": "LOT-20250115-0001",
      "dateEntree": "2025-01-15",
      "quantiteInitiale": 100,
      "quantiteRestante": 90,
      "prixUnitaire": 150.00
    },
    {
      "id": 2,
      "numeroLot": "LOT-20250115-0002",
      "dateEntree": "2025-01-15",
      "quantiteInitiale": 50,
      "quantiteRestante": 50,
      "prixUnitaire": 150.00
    }
  ]
}
```

### Historique des mouvements
```http
GET {{getURL}}/api/v1/stock/mouvements
```

### Mouvements d'un produit spécifique
```http
GET {{getURL}}/api/v1/stock/mouvements/produit/5
```

**Réponse:**
```json
[
  {
    "id": 15,
    "dateMouvement": "2025-01-15",
    "typeMouvement": "SORTIE",
    "quantite": -10,
    "prixUnitaire": 150.00,
    "reference": "BS-20250115-0002",
    "produitId": 5,
    "produitNom": "Souris Sans Fil Logitech",
    "lotId": 1,
    "lotNumero": "LOT-20250115-0001"
  },
  {
    "id": 10,
    "dateMouvement": "2025-01-15",
    "typeMouvement": "ENTREE",
    "quantite": 100,
    "prixUnitaire": 150.00,
    "reference": "CMD-1762870637782",
    "produitId": 5,
    "produitNom": "Souris Sans Fil Logitech",
    "lotId": 1,
    "lotNumero": "LOT-20250115-0001"
  }
]
```

### Produits en alerte
```http
GET {{getURL}}/api/v1/stock/alertes
```

### Valorisation totale du stock
```http
GET {{getURL}}/api/v1/stock/valorisation
```

**Réponse:**
```json
{
  "valeurTotale": 125000.00,
  "nombreProduits": 7,
  "quantiteTotale": 450
}
```

---

## 🔄 Scénario complet de test

### Scénario 1: Approvisionnement complet

#### 1. Créer un fournisseur
```http
POST {{getURL}}/api/v1/fournisseurs
{
  "raisonSociale": "TechnoPlus SARL",
  "ice": "002345678901234",
  ...
}
```

#### 2. Créer un produit
```http
POST {{getURL}}/api/v1/produits
{
  "reference": "SOURIS-001",
  "nom": "Souris Sans Fil Logitech",
  "stockActuel": 0,
  "pointCommande": 10,
  ...
}
```

#### 3. Créer une commande
```http
POST {{getURL}}/api/v1/commandes
{
  "fournisseurId": 1,
  "lignes": [
    {
      "produitId": 5,
      "quantite": 100,
      "prixUnitaire": 150.00
    }
  ]
}
```

#### 4. Valider la commande
```http
PATCH {{getURL}}/api/v1/commandes/11/statut?statut=VALIDEE
```

#### 5. Réceptionner la commande
```http
PUT {{getURL}}/api/v1/commandes/11/reception
```

#### 6. Vérifier le stock
```http
GET {{getURL}}/api/v1/stock/produit/5
```

---

### Scénario 2: Sortie de stock

#### 1. Créer un bon de sortie
```http
POST {{getURL}}/api/v1/bons-sortie
{
  "atelier": "ATELIER_A",
  "lignes": [
    {
      "produitId": 5,
      "quantite": 10
    }
  ]
}
```

#### 2. Valider le bon
```http
PUT {{getURL}}/api/v1/bons-sortie/2/valider
```

#### 3. Vérifier les mouvements
```http
GET {{getURL}}/api/v1/stock/mouvements/produit/5
```

#### 4. Vérifier le stock mis à jour
```http
GET {{getURL}}/api/v1/stock/produit/5
```

---

## ⚠️ Erreurs courantes et solutions

### 1. "Stock insuffisant"
**Cause:** Aucun lot disponible pour le produit  
**Solution:** Créer une commande, la valider et la réceptionner

### 2. "Seules les commandes VALIDÉES peuvent être réceptionnées"
**Cause:** La commande est au statut EN_ATTENTE  
**Solution:** Changer le statut à VALIDEE avec PATCH

### 3. "Duplicate entry for key 'ice'"
**Cause:** Un fournisseur avec cet ICE existe déjà  
**Solution:** Utiliser un ICE différent

### 4. "Column 'statut' cannot be null"
**Cause:** Le statut n'est pas fourni (bug corrigé)  
**Solution:** Le statut est maintenant généré automatiquement

### 5. "Seuls les bons BROUILLON peuvent être modifiés"
**Cause:** Tentative de modification d'un bon validé  
**Solution:** Seuls les bons BROUILLON sont modifiables

---

## 📊 Règles métier importantes

### Gestion FIFO
- Les sorties consomment toujours les lots les plus anciens en premier
- Un lot est identifié par sa date d'entrée
- La quantité restante d'un lot diminue à chaque sortie

### Statuts des commandes
- `EN_ATTENTE` → `VALIDEE` → `LIVREE`
- Seules les commandes VALIDÉES peuvent être réceptionnées
- Une commande LIVREE ne peut plus être modifiée

### Statuts des bons de sortie
- `BROUILLON` → `VALIDE` ou `ANNULE`
- Seuls les bons BROUILLON peuvent être modifiés
- Un bon VALIDE ne peut pas être annulé

### Alertes de stock
- Un produit est en alerte si `stockActuel <= pointCommande`
- Les alertes sont calculées automatiquement

---

## 🚀 Démarrage du projet

### Prérequis
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Configuration
1. Créer la base de données:
```sql
CREATE DATABASE gestion_stock;
```

2. Configurer `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_stock
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Lancer l'application:
```bash
mvn spring-boot:run
```

4. L'API sera disponible sur: `http://localhost:8080/tricol-stock`

---

## 📝 Notes importantes

- Tous les numéros (commandes, bons, lots) sont générés automatiquement
- Les dates sont au format ISO: `YYYY-MM-DD`
- Les montants sont en BigDecimal avec 2 décimales
- Les IDs sont auto-incrémentés
- La validation des données est automatique via `@Valid`

---

## 🔗 Endpoints récapitulatifs

| Module | Endpoint | Méthode | Description |
|--------|----------|---------|-------------|
| Fournisseurs | `/api/v1/fournisseurs` | GET | Liste tous |
| | `/api/v1/fournisseurs/{id}` | GET | Détails |
| | `/api/v1/fournisseurs` | POST | Créer |
| | `/api/v1/fournisseurs/{id}` | PUT | Modifier |
| | `/api/v1/fournisseurs/{id}` | DELETE | Supprimer |
| Produits | `/api/v1/produits` | GET | Liste tous |
| | `/api/v1/produits/{id}` | GET | Détails |
| | `/api/v1/produits` | POST | Créer |
| | `/api/v1/produits/{id}` | PUT | Modifier |
| | `/api/v1/produits/{id}` | DELETE | Supprimer |
| | `/api/v1/produits/alertes` | GET | En alerte |
| | `/api/v1/produits/{id}/stock` | GET | Stock |
| Commandes | `/api/v1/commandes` | GET | Liste toutes |
| | `/api/v1/commandes/{id}` | GET | Détails |
| | `/api/v1/commandes` | POST | Créer |
| | `/api/v1/commandes/{id}` | PUT | Modifier |
| | `/api/v1/commandes/{id}` | DELETE | Supprimer |
| | `/api/v1/commandes/{id}/statut` | PATCH | Changer statut |
| | `/api/v1/commandes/{id}/reception` | PUT | Réceptionner |
| Bons Sortie | `/api/v1/bons-sortie` | GET | Liste tous |
| | `/api/v1/bons-sortie/{id}` | GET | Détails |
| | `/api/v1/bons-sortie` | POST | Créer |
| | `/api/v1/bons-sortie/{id}` | PUT | Modifier |
| | `/api/v1/bons-sortie/{id}/valider` | PUT | Valider |
| | `/api/v1/bons-sortie/{id}/annuler` | PUT | Annuler |
| Stock | `/api/v1/stock` | GET | État global |
| | `/api/v1/stock/produit/{id}` | GET | Détail produit |
| | `/api/v1/stock/mouvements` | GET | Historique |
| | `/api/v1/stock/mouvements/produit/{id}` | GET | Mouvements produit |
| | `/api/v1/stock/alertes` | GET | Alertes |
| | `/api/v1/stock/valorisation` | GET | Valorisation |

---

**Projet développé avec ❤️ par Tricol Stock Team**
