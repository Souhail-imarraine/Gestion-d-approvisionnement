# Guide Complet - Système de Gestion d'Approvisionnement

## Table des Matières
1. [Vue d'ensemble du projet](#vue-densemble-du-projet)
2. [Concepts techniques utilisés](#concepts-techniques-utilisés)
3. [Architecture du projet](#architecture-du-projet)
4. [Cas d'usage complet](#cas-dusage-complet)
5. [Tests des endpoints](#tests-des-endpoints)

---

## Vue d'ensemble du projet

### Objectif
Système de gestion d'approvisionnement et de stock pour une entreprise industrielle permettant de:
- Gérer les fournisseurs
- Créer et suivre les commandes
- Recevoir les marchandises
- Gérer les sorties de stock (bons de sortie)
- Suivre les mouvements de stock en temps réel
- Valoriser le stock selon la méthode FIFO

### Technologies utilisées
- **Spring Boot 3.5.7** - Framework principal
- **Java 17** - Langage de programmation
- **MySQL/MariaDB** - Base de données
- **Liquibase** - Gestion des migrations de base de données
- **MapStruct** - Mapping automatique entre entités et DTOs
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances
- **Hibernate/JPA** - ORM pour la persistance

---

## Concepts techniques utilisés

### 1. Architecture en couches (Layered Architecture)

```
Controller Layer (API REST)
    ↓
Service Layer (Logique métier)
    ↓
Repository Layer (Accès aux données)
    ↓
Database (MySQL)
```

**Avantages:**
- Séparation des responsabilités
- Facilité de maintenance
- Testabilité améliorée

### 2. Pattern DTO (Data Transfer Object)

**Structure:**
```
dto/
├── request/          # DTOs pour les requêtes entrantes
│   ├── FournisseurCreateRequest
│   ├── FournisseurUpdateRequest
│   └── ...
└── response/         # DTOs pour les réponses sortantes
    ├── FournisseurResponseDTO
    ├── CommandeResponseDTO
    └── ...
```

**Pourquoi?**
- Sécurité: Ne pas exposer les entités directement
- Validation: Contrôle des données entrantes avec `@Valid`
- Flexibilité: Différentes structures pour création/mise à jour/lecture

### 3. MapStruct - Mapping automatique

**Exemple:**
```java
@Mapper(componentModel = "spring")
public interface FournisseurMapper {
    FournisseurResponseDTO toResponseDTO(Fournisseur entity);
    
    @Mapping(target = "id", ignore = true)
    Fournisseur toEntity(FournisseurCreateRequest dto);
}
```

**Avantages:**
- Génération de code à la compilation
- Performance optimale
- Réduction du code manuel

### 4. Liquibase - Gestion des migrations

**Structure:**
```
db/changelog/
├── db.changelog-master.xml    # Fichier principal
├── 001-create-fournisseurs.xml
├── 002-create-produits.xml
├── 003-create-commandes.xml
└── ...
```

**Avantages:**
- Versioning de la base de données
- Déploiement automatique des changements
- Rollback possible
- Traçabilité complète

### 5. Strategy Pattern - FIFO Stock

**Implémentation:**
```java
@Component
public class FifoStockStrategy {
    public List<MouvementStock> consumeStock(Produit produit, Integer quantity) {
        // Consomme les lots les plus anciens en premier
        List<LotStock> lots = lotStockRepository
            .findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(...);
        // ...
    }
}
```

**Principe FIFO (First In, First Out):**
- Les premiers lots entrés sont les premiers sortis
- Évite la péremption des produits
- Valorisation cohérente du stock

### 6. Enums pour les statuts

```java
public enum StatutCommande {
    EN_ATTENTE,    // Commande créée
    VALIDEE,       // Commande validée
    LIVREE         // Commande reçue
}

public enum StatutBonSortie {
    BROUILLON,     // Modifiable
    VALIDE,        // Validé, stock consommé
    ANNULE         // Annulé
}
```

**Avantages:**
- Type-safe
- Évite les erreurs de saisie
- Facilite les transitions d'état

### 7. Validation avec Bean Validation

```java
public class FournisseurCreateRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;
    
    @Email(message = "Email invalide")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String telephone;
}
```

### 8. Gestion des exceptions personnalisées

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### 9. Transactions avec @Transactional

```java
@Transactional
public CommandeResponseDTO create(CommandeCreateRequest dto) {
    // Toutes les opérations sont atomiques
    // Si une échoue, tout est annulé (rollback)
}
```

---

## Architecture du projet

### Structure des packages

```
com.tricol.stock/
├── controller/          # API REST endpoints
├── service/            # Logique métier
│   └── impl/          # Implémentations
├── repository/         # Accès aux données (JPA)
├── entity/            # Entités JPA
├── dto/               # Data Transfer Objects
│   ├── request/      # DTOs entrantes
│   └── response/     # DTOs sortantes
├── mapper/            # MapStruct mappers
├── enums/             # Énumérations
├── exception/         # Exceptions personnalisées
└── config/            # Configurations
```

### Modèle de données

```
Fournisseur (1) -----> (*) Commande
                            ↓
                       LigneCommande (*) -----> (1) Produit
                            ↓
                       Reception
                            ↓
                       LotStock -----> MouvementStock
                            ↑
BonSortie (*) -----> (*) LigneBonSortie -----> (1) Produit
```

---

## Cas d'usage complet

### Scénario: Approvisionnement complet d'un produit

**Acteurs:**
- Gestionnaire d'approvisionnement
- Magasinier

**Étapes:**

1. **Créer un fournisseur**
2. **Créer un produit**
3. **Créer une commande** auprès du fournisseur
4. **Valider la commande**
5. **Recevoir la commande** (création de lots de stock)
6. **Vérifier le stock**
7. **Créer un bon de sortie** pour un atelier
8. **Valider le bon de sortie** (consommation FIFO du stock)
9. **Consulter les mouvements de stock**
10. **Consulter la valorisation du stock**

---

## Tests des endpoints

### Configuration de base

**Base URL:** `http://localhost:8081/tricol-stock`

**Headers:**
```
Content-Type: application/json
Accept: application/json
```

---

### ÉTAPE 1: Créer un fournisseur

**Endpoint:** `POST /api/v1/fournisseurs`

**Request Body:**
```json
{
  "nom": "Fournisseur Acier SA",
  "adresse": "123 Rue de l'Industrie, Casablanca",
  "telephone": "+212612345678",
  "email": "contact@acier-sa.ma",
  "delaiLivraison": 7
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nom": "Fournisseur Acier SA",
  "adresse": "123 Rue de l'Industrie, Casablanca",
  "telephone": "+212612345678",
  "email": "contact@acier-sa.ma",
  "delaiLivraison": 7
}
```

**Concepts utilisés:**
- DTO Request/Response pattern
- Validation automatique avec `@Valid`
- MapStruct pour la conversion Entity ↔ DTO
- Auto-génération de l'ID par la base de données

---

### ÉTAPE 2: Créer un produit

**Endpoint:** `POST /api/v1/produits`

**Request Body:**
```json
{
  "reference": "ACIER-001",
  "nom": "Tôle d'acier 2mm",
  "description": "Tôle d'acier galvanisé épaisseur 2mm",
  "prixUnitaire": 150.00,
  "categorie": "MATIERE_PREMIERE",
  "pointCommande": 50,
  "uniteMesure": "KG"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "reference": "ACIER-001",
  "nom": "Tôle d'acier 2mm",
  "description": "Tôle d'acier galvanisé épaisseur 2mm",
  "prixUnitaire": 150.00,
  "categorie": "MATIERE_PREMIERE",
  "stockActuel": 0,
  "pointCommande": 50,
  "uniteMesure": "KG"
}
```

**Concepts utilisés:**
- `stockActuel` initialisé à 0 automatiquement
- Validation de l'unicité de la référence
- `pointCommande`: seuil d'alerte pour réapprovisionnement

---

### ÉTAPE 3: Créer une commande

**Endpoint:** `POST /api/v1/commandes`

**Request Body:**
```json
{
  "fournisseurId": 1,
  "dateLivraisonPrevue": "2025-11-21",
  "lignes": [
    {
      "produitId": 1,
      "quantite": 100,
      "prixUnitaire": 150.00
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "numero": "CMD-1731576000000",
  "dateCommande": "2025-11-14",
  "dateLivraisonPrevue": "2025-11-21",
  "statut": "EN_ATTENTE",
  "montantTotal": 15000.00,
  "fournisseur": {
    "id": 1,
    "nom": "Fournisseur Acier SA",
    "telephone": "+212612345678",
    "email": "contact@acier-sa.ma"
  },
  "lignes": [
    {
      "id": 1,
      "produit": {
        "id": 1,
        "reference": "ACIER-001",
        "nom": "Tôle d'acier 2mm"
      },
      "quantite": 100,
      "prixUnitaire": 150.00,
      "sousTotal": 15000.00
    }
  ]
}
```

**Concepts utilisés:**
- Auto-génération du numéro de commande: `CMD-{timestamp}`
- Statut initial: `EN_ATTENTE`
- Calcul automatique du `montantTotal`
- Calcul automatique du `sousTotal` par ligne
- Transaction atomique: si une ligne échoue, toute la commande est annulée

---

### ÉTAPE 4: Valider la commande

**Endpoint:** `PUT /api/v1/commandes/1/statut?nouveauStatut=VALIDEE`

**Response (200 OK):**
```json
{
  "id": 1,
  "numero": "CMD-1731576000000",
  "statut": "VALIDEE",
  ...
}
```

**Concepts utilisés:**
- Transition d'état: `EN_ATTENTE` → `VALIDEE`
- Seules les commandes `VALIDEE` peuvent être reçues

---

### ÉTAPE 5: Recevoir la commande

**Endpoint:** `POST /api/v1/receptions`

**Request Body:**
```json
{
  "commandeId": 1,
  "dateReception": "2025-11-20",
  "numeroLivraison": "LIV-2025-001",
  "lignes": [
    {
      "ligneCommandeId": 1,
      "quantiteRecue": 100
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "commande": {
    "id": 1,
    "numero": "CMD-1731576000000",
    "statut": "LIVREE"
  },
  "dateReception": "2025-11-20",
  "numeroLivraison": "LIV-2025-001",
  "lignes": [
    {
      "id": 1,
      "produit": {
        "id": 1,
        "reference": "ACIER-001",
        "nom": "Tôle d'acier 2mm"
      },
      "quantiteCommandee": 100,
      "quantiteRecue": 100
    }
  ]
}
```

**Concepts utilisés:**
- Changement automatique du statut de la commande: `VALIDEE` → `LIVREE`
- **Création automatique d'un lot de stock (LotStock)**
- **Création automatique d'un mouvement de stock (MouvementStock) de type ENTREE**
- **Mise à jour du stock actuel du produit**
- Transaction atomique: tout ou rien

**Ce qui se passe en arrière-plan:**

1. **Création LotStock:**
```java
LotStock lot = new LotStock();
lot.setProduit(produit);
lot.setNumeroLot("LOT-" + timestamp);
lot.setQuantiteInitiale(100);
lot.setQuantiteRestante(100);
lot.setDateEntree(LocalDate.now());
lot.setPrixUnitaire(150.00);
```

2. **Création MouvementStock:**
```java
MouvementStock mouvement = new MouvementStock();
mouvement.setProduit(produit);
mouvement.setLot(lot);
mouvement.setTypeMouvement(TypeMouvement.ENTREE);
mouvement.setQuantite(100);
mouvement.setPrixUnitaire(150.00);
mouvement.setDateMouvement(LocalDate.now());
```

3. **Mise à jour du stock:**
```java
produit.setStockActuel(produit.getStockActuel() + 100); // 0 + 100 = 100
```

---

### ÉTAPE 6: Vérifier le stock

**Endpoint:** `GET /api/v1/stock/produit/1`

**Response (200 OK):**
```json
{
  "produit": {
    "id": 1,
    "reference": "ACIER-001",
    "nom": "Tôle d'acier 2mm"
  },
  "stockActuel": 100,
  "stockDisponible": 100,
  "stockReserve": 0,
  "valeurStock": 15000.00,
  "prixMoyenPondere": 150.00
}
```

**Endpoint:** `GET /api/v1/stock/lots/produit/1`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "numeroLot": "LOT-1731576000000",
    "produit": {
      "id": 1,
      "reference": "ACIER-001",
      "nom": "Tôle d'acier 2mm"
    },
    "quantiteInitiale": 100,
    "quantiteRestante": 100,
    "dateEntree": "2025-11-20",
    "prixUnitaire": 150.00
  }
]
```

---

### ÉTAPE 7: Créer un bon de sortie

**Endpoint:** `POST /api/v1/bons-sortie`

**Request Body:**
```json
{
  "atelier": "ATELIER_PRODUCTION",
  "motif": "Production série A",
  "lignes": [
    {
      "produitId": 1,
      "quantite": 30
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "numero": "BS-20251114",
  "dateCreation": "2025-11-14",
  "atelier": "ATELIER_PRODUCTION",
  "motif": "Production série A",
  "statut": "BROUILLON",
  "lignes": [
    {
      "id": 1,
      "produit": {
        "id": 1,
        "reference": "ACIER-001",
        "nom": "Tôle d'acier 2mm"
      },
      "quantite": 30
    }
  ]
}
```

**Concepts utilisés:**
- Auto-génération du numéro: `BS-{YYYYMMDD}`
- Statut initial: `BROUILLON` (modifiable)
- Le stock n'est PAS encore consommé

---

### ÉTAPE 8: Valider le bon de sortie

**Endpoint:** `PUT /api/v1/bons-sortie/1/valider`

**Response (200 OK):**
```json
{
  "id": 1,
  "numero": "BS-20251114",
  "dateCreation": "2025-11-14",
  "dateValidation": "2025-11-14",
  "atelier": "ATELIER_PRODUCTION",
  "motif": "Production série A",
  "statut": "VALIDE",
  "lignes": [
    {
      "id": 1,
      "produit": {
        "id": 1,
        "reference": "ACIER-001",
        "nom": "Tôle d'acier 2mm"
      },
      "quantite": 30
    }
  ]
}
```

**Concepts utilisés - FIFO Strategy:**

1. **Recherche des lots disponibles** (ordre chronologique):
```java
List<LotStock> lots = lotStockRepository
    .findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1, 0);
```

2. **Consommation du lot le plus ancien:**
```java
LotStock lot = lots.get(0); // LOT-1731576000000
lot.setQuantiteRestante(100 - 30); // 70
```

3. **Création du mouvement de sortie:**
```java
MouvementStock mouvement = new MouvementStock();
mouvement.setTypeMouvement(TypeMouvement.SORTIE);
mouvement.setQuantite(30);
mouvement.setPrixUnitaire(150.00); // Prix du lot
mouvement.setLot(lot);
```

4. **Mise à jour du stock produit:**
```java
produit.setStockActuel(100 - 30); // 70
```

**Vérification après validation:**

`GET /api/v1/produits/1`
```json
{
  "id": 1,
  "stockActuel": 70,
  ...
}
```

`GET /api/v1/stock/lots/produit/1`
```json
[
  {
    "id": 1,
    "numeroLot": "LOT-1731576000000",
    "quantiteInitiale": 100,
    "quantiteRestante": 70,
    ...
  }
]
```

---

### ÉTAPE 9: Consulter les mouvements de stock

**Endpoint:** `GET /api/v1/stock/mouvements/produit/1`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "produit": {
      "id": 1,
      "reference": "ACIER-001",
      "nom": "Tôle d'acier 2mm"
    },
    "lot": {
      "id": 1,
      "numeroLot": "LOT-1731576000000"
    },
    "typeMouvement": "ENTREE",
    "quantite": 100,
    "prixUnitaire": 150.00,
    "dateMouvement": "2025-11-20"
  },
  {
    "id": 2,
    "produit": {
      "id": 1,
      "reference": "ACIER-001",
      "nom": "Tôle d'acier 2mm"
    },
    "lot": {
      "id": 1,
      "numeroLot": "LOT-1731576000000"
    },
    "typeMouvement": "SORTIE",
    "quantite": 30,
    "prixUnitaire": 150.00,
    "dateMouvement": "2025-11-14"
  }
]
```

**Concepts utilisés:**
- Traçabilité complète des mouvements
- Lien avec le lot d'origine
- Historique chronologique

---

### ÉTAPE 10: Consulter la valorisation du stock

**Endpoint:** `GET /api/v1/stock/valorisation`

**Response (200 OK):**
```json
[
  {
    "produit": {
      "id": 1,
      "reference": "ACIER-001",
      "nom": "Tôle d'acier 2mm"
    },
    "quantiteStock": 70,
    "prixMoyenPondere": 150.00,
    "valeurTotale": 10500.00
  }
]
```

**Calcul de la valorisation:**
```
Valeur totale = Quantité restante × Prix moyen pondéré
              = 70 × 150.00
              = 10 500.00 MAD
```

---

## Endpoints supplémentaires

### Gestion des fournisseurs

```
GET    /api/v1/fournisseurs           # Liste tous les fournisseurs
GET    /api/v1/fournisseurs/{id}      # Détails d'un fournisseur
PUT    /api/v1/fournisseurs/{id}      # Modifier un fournisseur
DELETE /api/v1/fournisseurs/{id}      # Supprimer un fournisseur
```

### Gestion des produits

```
GET    /api/v1/produits               # Liste tous les produits
GET    /api/v1/produits/{id}          # Détails d'un produit
PUT    /api/v1/produits/{id}          # Modifier un produit
DELETE /api/v1/produits/{id}          # Supprimer un produit
GET    /api/v1/produits/reference/{ref} # Recherche par référence
```

### Gestion des commandes

```
GET    /api/v1/commandes              # Liste toutes les commandes
GET    /api/v1/commandes/{id}         # Détails d'une commande
PUT    /api/v1/commandes/{id}         # Modifier une commande
DELETE /api/v1/commandes/{id}         # Supprimer une commande
GET    /api/v1/commandes/statut/{statut} # Filtrer par statut
GET    /api/v1/commandes/fournisseur/{id} # Commandes d'un fournisseur
```

### Gestion des bons de sortie

```
GET    /api/v1/bons-sortie            # Liste tous les bons
GET    /api/v1/bons-sortie/{id}       # Détails d'un bon
PUT    /api/v1/bons-sortie/{id}       # Modifier un bon (si BROUILLON)
PUT    /api/v1/bons-sortie/{id}/annuler # Annuler un bon
GET    /api/v1/bons-sortie/atelier/{atelier} # Bons d'un atelier
```

### Gestion du stock

```
GET    /api/v1/stock/produit/{id}     # État du stock d'un produit
GET    /api/v1/stock/lots/produit/{id} # Lots d'un produit
GET    /api/v1/stock/mouvements/produit/{id} # Mouvements d'un produit
GET    /api/v1/stock/valorisation     # Valorisation globale
GET    /api/v1/stock/alertes          # Produits sous le point de commande
```

---

## Règles métier importantes

### 1. Workflow des commandes
```
EN_ATTENTE → VALIDEE → LIVREE
```
- Une commande doit être `VALIDEE` avant d'être reçue
- Une fois `LIVREE`, elle ne peut plus être modifiée

### 2. Workflow des bons de sortie
```
BROUILLON → VALIDE
BROUILLON → ANNULE
```
- Seuls les bons `BROUILLON` peuvent être modifiés
- La validation consomme le stock (FIFO)
- Un bon `VALIDE` ne peut plus être modifié

### 3. Gestion du stock
- Le stock est mis à jour automatiquement lors des réceptions et validations de bons de sortie
- La méthode FIFO garantit que les lots les plus anciens sont consommés en premier
- Les mouvements de stock sont tracés pour chaque opération

### 4. Contraintes d'intégrité
- Un fournisseur avec des commandes ne peut pas être supprimé
- Un produit avec des commandes ne peut pas être supprimé
- La référence d'un produit doit être unique
- Les quantités doivent être positives

---

## Scénario de test complet avec Postman

### Collection Postman

1. **Setup**
   - Créer 2 fournisseurs
   - Créer 3 produits

2. **Commande 1**
   - Créer commande (Fournisseur 1, Produit 1: 100 unités)
   - Valider commande
   - Recevoir commande

3. **Commande 2**
   - Créer commande (Fournisseur 1, Produit 1: 50 unités)
   - Valider commande
   - Recevoir commande

4. **Vérification stock**
   - Stock total = 150 unités
   - 2 lots différents

5. **Bon de sortie 1**
   - Créer bon (Produit 1: 120 unités)
   - Valider bon
   - Vérifier: Lot 1 consommé (100), Lot 2 partiellement consommé (20)

6. **Vérification finale**
   - Stock restant = 30 unités
   - Lot 1: quantiteRestante = 0
   - Lot 2: quantiteRestante = 30
   - Mouvements: 2 ENTREE + 2 SORTIE

---

## Conclusion

Ce système implémente un workflow complet de gestion d'approvisionnement avec:
- Architecture propre et maintenable
- Traçabilité complète des opérations
- Gestion FIFO du stock
- Validation des données
- Transactions atomiques
- API REST complète et documentée
