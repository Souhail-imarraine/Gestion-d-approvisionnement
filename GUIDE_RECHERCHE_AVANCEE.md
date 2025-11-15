# Guide Complet - Recherche Avancée avec Spring Data JPA Specifications

## Table des Matières
1. [Concepts Nouveaux](#concepts-nouveaux)
2. [Implémentation Step-by-Step](#implémentation-step-by-step)
3. [Tests des Endpoints](#tests-des-endpoints)

---

## Partie 2: Recherche Avancée sur les Mouvements de Stock

### Objectif
Implémenter une API de recherche multi-critères pour l'audit des mouvements de stock avec pagination.

---

## Concepts Nouveaux à Maîtriser

### 1. Spring Data JPA Specifications

**Qu'est-ce que c'est?**
Une API qui permet de construire dynamiquement des requêtes SQL complexes de manière programmatique.

**Pourquoi l'utiliser?**
- Requêtes dynamiques basées sur des critères optionnels
- Code réutilisable et maintenable
- Type-safe (pas de String SQL)
- Combinaison de critères avec AND/OR

**Exemple simple:**
```java
public class MouvementStockSpecification {
    
    public static Specification<MouvementStock> hasProduitId(Long produitId) {
        return (root, query, criteriaBuilder) -> {
            if (produitId == null) {
                return null; // Pas de filtre
            }
            return criteriaBuilder.equal(root.get("produit").get("id"), produitId);
        };
    }
}
```

### 2. Criteria API

**Composants principaux:**
- `Root<T>` - Représente l'entité racine (FROM)
- `CriteriaQuery` - Représente la requête complète
- `CriteriaBuilder` - Construit les prédicats (WHERE)
- `Predicate` - Condition de filtrage

**Exemple:**
```java
// SQL équivalent: WHERE produit.id = ?
criteriaBuilder.equal(root.get("produit").get("id"), produitId)

// SQL équivalent: WHERE date BETWEEN ? AND ?
criteriaBuilder.between(root.get("dateMouvement"), dateDebut, dateFin)
```

### 3. Pagination avec Spring Data

**Interface Pageable:**
```java
Pageable pageable = PageRequest.of(page, size);
Page<MouvementStock> result = repository.findAll(spec, pageable);
```

**Objet Page:**
```java
page.getContent();        // Liste des éléments
page.getTotalElements();  // Nombre total d'éléments
page.getTotalPages();     // Nombre total de pages
page.getNumber();         // Numéro de page actuelle
page.getSize();           // Taille de la page
```

---

## Implémentation Step-by-Step

### Étape 1: Créer la classe Specification

**Fichier:** `MouvementStockSpecification.java`

```java
package com.tricol.stock.specification;

import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.enums.TypeMouvement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MouvementStockSpecification {
    
    // Filtre par ID de produit
    public static Specification<MouvementStock> hasProduitId(Long produitId) {
        return (root, query, criteriaBuilder) -> {
            if (produitId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("produit").get("id"), produitId);
        };
    }
    
    // Filtre par référence de produit
    public static Specification<MouvementStock> hasProduitReference(String reference) {
        return (root, query, criteriaBuilder) -> {
            if (reference == null || reference.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("produit").get("reference"), reference);
        };
    }
    
    // Filtre par type de mouvement (ENTREE/SORTIE)
    public static Specification<MouvementStock> hasTypeMouvement(TypeMouvement type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("typeMouvement"), type);
        };
    }
    
    // Filtre par numéro de lot
    public static Specification<MouvementStock> hasNumeroLot(String numeroLot) {
        return (root, query, criteriaBuilder) -> {
            if (numeroLot == null || numeroLot.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("lot").get("numeroLot"), numeroLot);
        };
    }
    
    // Filtre par période (date début)
    public static Specification<MouvementStock> hasDateMouvementAfter(LocalDate dateDebut) {
        return (root, query, criteriaBuilder) -> {
            if (dateDebut == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("dateMouvement"), dateDebut);
        };
    }
    
    // Filtre par période (date fin)
    public static Specification<MouvementStock> hasDateMouvementBefore(LocalDate dateFin) {
        return (root, query, criteriaBuilder) -> {
            if (dateFin == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dateMouvement"), dateFin);
        };
    }
}
```

---

### Étape 2: Modifier le Repository

**Fichier:** `MouvementStockRepository.java`

```java
package com.tricol.stock.repository;

import com.tricol.stock.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MouvementStockRepository extends 
    JpaRepository<MouvementStock, Long>, 
    JpaSpecificationExecutor<MouvementStock> {
    
    // JpaSpecificationExecutor ajoute automatiquement:
    // - Page<T> findAll(Specification<T> spec, Pageable pageable)
    // - List<T> findAll(Specification<T> spec)
}
```

---

### Étape 3: Créer le DTO de Recherche

**Fichier:** `MouvementStockSearchRequest.java`

```java
package com.tricol.stock.dto.request;

import com.tricol.stock.enums.TypeMouvement;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class MouvementStockSearchRequest {
    
    private Long produitId;
    
    private String reference;
    
    private TypeMouvement type;
    
    private String numeroLot;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;
    
    private Integer page = 0;
    
    private Integer size = 10;
}
```

---

### Étape 4: Implémenter le Service

**Fichier:** `StockService.java` (interface)

```java
Page<MouvementStockResponseDTO> rechercherMouvements(
    MouvementStockSearchRequest request
);
```

**Fichier:** `StockServiceImpl.java`

```java
@Override
public Page<MouvementStockResponseDTO> rechercherMouvements(MouvementStockSearchRequest request) {
    
    // Construire la specification en combinant tous les critères
    Specification<MouvementStock> spec = Specification
        .where(MouvementStockSpecification.hasProduitId(request.getProduitId()))
        .and(MouvementStockSpecification.hasProduitReference(request.getReference()))
        .and(MouvementStockSpecification.hasTypeMouvement(request.getType()))
        .and(MouvementStockSpecification.hasNumeroLot(request.getNumeroLot()))
        .and(MouvementStockSpecification.hasDateMouvementAfter(request.getDateDebut()))
        .and(MouvementStockSpecification.hasDateMouvementBefore(request.getDateFin()));
    
    // Créer l'objet Pageable
    Pageable pageable = PageRequest.of(
        request.getPage(), 
        request.getSize(),
        Sort.by("dateMouvement").descending() // Tri par date décroissante
    );
    
    // Exécuter la requête
    Page<MouvementStock> page = mouvementStockRepository.findAll(spec, pageable);
    
    // Convertir en DTO
    return page.map(mouvementStockMapper::toResponseDTO);
}
```

---

### Étape 5: Créer le Controller

**Fichier:** `StockController.java`

```java
@GetMapping("/mouvements")
public ResponseEntity<Page<MouvementStockResponseDTO>> rechercherMouvements(
    @RequestParam(required = false) Long produitId,
    @RequestParam(required = false) String reference,
    @RequestParam(required = false) TypeMouvement type,
    @RequestParam(required = false) String numeroLot,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
    @RequestParam(defaultValue = "0") Integer page,
    @RequestParam(defaultValue = "10") Integer size
) {
    MouvementStockSearchRequest request = new MouvementStockSearchRequest();
    request.setProduitId(produitId);
    request.setReference(reference);
    request.setType(type);
    request.setNumeroLot(numeroLot);
    request.setDateDebut(dateDebut);
    request.setDateFin(dateFin);
    request.setPage(page);
    request.setSize(size);
    
    Page<MouvementStockResponseDTO> result = stockService.rechercherMouvements(request);
    return ResponseEntity.ok(result);
}
```

---

## Tests des Endpoints

### 1. Recherche par Produit et Type avec Pagination

**Request:**
```http
GET http://localhost:8081/tricol-stock/api/v1/stock/mouvements?produitId=1&type=SORTIE&page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 5,
      "produit": {
        "id": 1,
        "reference": "ACIER-001",
        "nom": "Tôle d'acier 2mm"
      },
      "typeMouvement": "SORTIE",
      "quantite": 30,
      "prixUnitaire": 150.00,
      "dateMouvement": "2025-11-14"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

---

### 2. Recherche par Période

**Request:**
```http
GET http://localhost:8081/tricol-stock/api/v1/stock/mouvements?dateDebut=2025-01-01&dateFin=2025-03-31
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "typeMouvement": "ENTREE",
      "quantite": 100,
      "dateMouvement": "2025-01-15"
    },
    {
      "id": 2,
      "typeMouvement": "SORTIE",
      "quantite": 30,
      "dateMouvement": "2025-02-10"
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

---

### 3. Recherche par Numéro de Lot

**Request:**
```http
GET http://localhost:8081/tricol-stock/api/v1/stock/mouvements?numeroLot=LOT-2025-001
```

**Response:**
```json
{
  "content": [
    {
      "id": 3,
      "lot": {
        "id": 1,
        "numeroLot": "LOT-2025-001"
      },
      "typeMouvement": "ENTREE",
      "quantite": 100,
      "dateMouvement": "2025-01-20"
    }
  ],
  "totalElements": 1
}
```

---

### 4. Recherche Combinée Multi-Critères

**Request:**
```http
GET http://localhost:8081/tricol-stock/api/v1/stock/mouvements?reference=ACIER-001&type=ENTREE&dateDebut=2025-01-01&page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "produit": {
        "reference": "ACIER-001"
      },
      "typeMouvement": "ENTREE",
      "quantite": 100,
      "dateMouvement": "2025-01-15"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 5. Recherche avec Pagination (Page 2)

**Request:**
```http
GET http://localhost:8081/tricol-stock/api/v1/stock/mouvements?page=1&size=5
```

**Response:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 1,
    "pageSize": 5
  },
  "totalElements": 15,
  "totalPages": 3,
  "first": false,
  "last": false
}
```

---

## Explication du Fonctionnement

### Comment Specification fonctionne?

**1. Construction dynamique:**
```java
Specification<MouvementStock> spec = Specification.where(null);

if (produitId != null) {
    spec = spec.and(hasProduitId(produitId));
}

if (type != null) {
    spec = spec.and(hasTypeMouvement(type));
}
```

**2. Génération SQL:**
```sql
SELECT * FROM mouvements_stock m
JOIN produits p ON m.produit_id = p.id
WHERE p.id = ?
  AND m.type_mouvement = ?
  AND m.date_mouvement >= ?
  AND m.date_mouvement <= ?
ORDER BY m.date_mouvement DESC
LIMIT 10 OFFSET 0
```

---

## Avantages de cette Approche

1. **Flexibilité:** Critères optionnels combinables
2. **Maintenabilité:** Code réutilisable et lisible
3. **Performance:** Requête optimisée par JPA
4. **Type-safe:** Pas de String SQL, erreurs à la compilation
5. **Pagination:** Gestion automatique par Spring Data

---

## Checklist Partie 2

- [ ] Créer MouvementStockSpecification
- [ ] Modifier MouvementStockRepository (extends JpaSpecificationExecutor)
- [ ] Créer MouvementStockSearchRequest DTO
- [ ] Implémenter rechercherMouvements dans StockService
- [ ] Créer endpoint GET /api/v1/stock/mouvements
- [ ] Tester recherche par produitId
- [ ] Tester recherche par référence
- [ ] Tester recherche par type
- [ ] Tester recherche par numéro de lot
- [ ] Tester recherche par période
- [ ] Tester recherche combinée
- [ ] Tester pagination (page 0, 1, 2)
- [ ] Vérifier tri par date décroissante

---

## Ressources Supplémentaires

**Documentation officielle:**
- [Spring Data JPA Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- [Criteria API](https://docs.oracle.com/javaee/7/tutorial/persistence-criteria.htm)
- [Pagination](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.special-parameters)

**Exemples de code:**
- Voir `StockServiceImpl.java` pour l'implémentation complète
- Voir `MouvementStockSpecification.java` pour tous les critères
