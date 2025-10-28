# 🚀 Étapes pour Créer le Projet Tricol Stock FIFO from Scratch

## 📋 Vue d'Ensemble

**Durée totale**: 6-8 heures  
**Ordre**: Suivre les étapes dans l'ordre exact  
**Prérequis**: Java 17, Maven, MySQL, IntelliJ IDEA

---

## 🎯 PHASE 1: INITIALISATION (30 min)

### Étape 1.1: Créer le Projet Spring Boot

**Option A: Via Spring Initializr Web**
1. Aller sur https://start.spring.io/
2. Configurer:
    - Project: **Maven**
    - Language: **Java**
    - Spring Boot: **3.2.0**
    - Group: **com.tricol**
    - Artifact: **tricol-stock**
    - Packaging: **WAR**
    - Java: **17**

3. Ajouter les dépendances:
    - Spring Web
    - Spring Data JPA
    - MySQL Driver
    - Validation
    - Lombok

4. Cliquer "Generate" → Télécharger le ZIP

**Option B: Via IntelliJ IDEA**
1. File → New → Project
2. Spring Initializr
3. Même configuration que ci-dessus

---

### Étape 1.2: Ouvrir le Projet

```bash
# Extraire le ZIP
cd tricol-stock

# Ouvrir avec IntelliJ
# File → Open → Sélectionner le dossier
```

---

### Étape 1.3: Configurer pom.xml

Ajouter les dépendances manquantes:

```xml
<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- Liquibase -->
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>

<!-- Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

Ajouter le plugin Maven Compiler:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

### Étape 1.4: Créer la Base de Données

```sql
CREATE DATABASE tricol_stock_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### Étape 1.5: Configurer application.properties

```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/tricol_stock_db
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.enabled=true

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Serveur
server.port=8080
server.servlet.context-path=/tricol-stock

# Logging
logging.level.com.tricol=DEBUG
```

---

### Étape 1.6: Tester le Démarrage

```bash
mvn clean compile
mvn spring-boot:run
```

✅ L'application doit démarrer sans erreur

---

## 📦 PHASE 2: ENUMS (15 min)

### Étape 2.1: Créer le Package

```
src/main/java/com/tricol/enums/
```

### Étape 2.2: Créer les Enums

**StatutCommande.java**
```java
package com.tricol.enums;

public enum StatutCommande {
    EN_ATTENTE,
    VALIDEE,
    LIVREE,
    ANNULEE
}
```

**TypeMouvement.java**
```java
package com.tricol.enums;

public enum TypeMouvement {
    ENTREE,
    SORTIE
}
```

**MotifSortie.java**
```java
package com.tricol.enums;

public enum MotifSortie {
    PRODUCTION,
    MAINTENANCE,
    AUTRE
}
```

**StatutBon.java**
```java
package com.tricol.enums;

public enum StatutBon {
    BROUILLON,
    VALIDE,
    ANNULE
}
```

---

## 🗄️ PHASE 3: ENTITIES (1h)

### Étape 3.1: Créer le Package

```
src/main/java/com/tricol/entity/
```

### Étape 3.2: Créer les Entities

**Ordre de création** (important pour les dépendances):

1. **Fournisseur.java**
2. **Produit.java**
3. **CommandeFournisseur.java**
4. **LigneCommande.java**
5. **LotStock.java**
6. **MouvementStock.java**
7. **BonSortie.java**
8. **LigneBonSortie.java**

**Exemple - Produit.java**:
```java
package com.tricol.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Produit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String reference;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double prixUnitaire;
    
    private String categorie;
    
    @Column(nullable = false)
    private Integer stockActuel = 0;
    
    private Integer pointCommande = 10;
    
    private String uniteMesure;
}
```

📝 **Voir GUIDE_CODE_EXAMPLES.java pour le code complet de toutes les entities**

---

## 🗃️ PHASE 4: LIQUIBASE (45 min)

### Étape 4.1: Créer la Structure

```
src/main/resources/db/changelog/
├── db.changelog-master.xml
└── changes/
    ├── 001-create-fournisseur.xml
    ├── 002-create-produit.xml
    ├── 003-create-commande.xml
    ├── 004-create-stock.xml
    └── 005-create-bon-sortie.xml
```

### Étape 4.2: Créer db.changelog-master.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <include file="db/changelog/changes/001-create-fournisseur.xml"/>
    <include file="db/changelog/changes/002-create-produit.xml"/>
    <include file="db/changelog/changes/003-create-commande.xml"/>
    <include file="db/changelog/changes/004-create-stock.xml"/>
    <include file="db/changelog/changes/005-create-bon-sortie.xml"/>
    
</databaseChangeLog>
```

### Étape 4.3: Créer les Changesets

📝 **Voir GUIDE_LIQUIBASE.xml pour tous les changesets**

### Étape 4.4: Tester Liquibase

```bash
mvn spring-boot:run
```

✅ Vérifier que les tables sont créées dans MySQL

---

## 🔌 PHASE 5: REPOSITORIES (20 min)

### Étape 5.1: Créer le Package

```
src/main/java/com/tricol/repository/
```

### Étape 5.2: Créer les Repositories

**Exemple - ProduitRepository.java**:
```java
package com.tricol.repository;

import com.tricol.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    
    boolean existsByReference(String reference);
    
    @Query("SELECT p FROM Produit p WHERE p.stockActuel <= p.pointCommande")
    List<Produit> findProduitsEnAlerte();
}
```

**Créer tous les repositories**:
1. FournisseurRepository
2. ProduitRepository
3. CommandeFournisseurRepository
4. LigneCommandeRepository
5. LotStockRepository ⭐
6. MouvementStockRepository
7. BonSortieRepository
8. LigneBonSortieRepository

---

## 🚨 PHASE 6: EXCEPTIONS (15 min)

### Étape 6.1: Créer le Package

```
src/main/java/com/tricol/exception/
```

### Étape 6.2: Créer les Exceptions

**ResourceNotFoundException.java**
```java
package com.tricol.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**StockInsuffisantException.java**
```java
package com.tricol.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }
}
```

**BusinessException.java**
```java
package com.tricol.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

### Étape 6.3: Créer ErrorResponse

```java
package com.tricol.exception;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Integer status;
    private String message;
    private LocalDateTime timestamp;
}
```

### Étape 6.4: Créer GlobalExceptionHandler

📝 **Voir GUIDE_CODE_EXAMPLES.java pour le code complet**

---

## 📝 PHASE 7: DTOs (45 min)

### Étape 7.1: Créer la Structure

```
src/main/java/com/tricol/dto/
├── request/
└── response/
```

### Étape 7.2: Créer les DTOs Request

**Exemple - ProduitRequestDTO.java**:
```java
package com.tricol.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRequestDTO {
    
    @NotBlank(message = "La référence est obligatoire")
    private String reference;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    private String description;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    private Double prixUnitaire;
    
    private String categorie;
    
    @Min(value = 0, message = "Le point de commande doit être positif")
    private Integer pointCommande;
    
    private String uniteMesure;
}
```

**Créer tous les DTOs Request**:
1. FournisseurRequestDTO
2. ProduitRequestDTO
3. CommandeRequestDTO
4. LigneCommandeRequestDTO
5. BonSortieRequestDTO
6. LigneBonSortieRequestDTO

### Étape 7.3: Créer les DTOs Response

**Créer tous les DTOs Response**:
1. FournisseurResponseDTO
2. ProduitResponseDTO
3. CommandeResponseDTO
4. StockResponseDTO
5. LotStockResponseDTO
6. MouvementStockResponseDTO
7. BonSortieResponseDTO
8. ValorisationStockDTO

---

## 🗺️ PHASE 8: MAPPERS (20 min)

### Étape 8.1: Créer le Package

```
src/main/java/com/tricol/mapper/
```

### Étape 8.2: Créer les Mappers

**Exemple - ProduitMapper.java**:
```java
package com.tricol.mapper;

import com.tricol.dto.request.ProduitRequestDTO;
import com.tricol.dto.response.ProduitResponseDTO;
import com.tricol.entity.Produit;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProduitMapper {
    
    @Mapping(target = "enAlerte", 
             expression = "java(produit.getStockActuel() <= produit.getPointCommande())")
    ProduitResponseDTO toResponseDTO(Produit produit);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockActuel", constant = "0")
    Produit toEntity(ProduitRequestDTO requestDTO);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockActuel", ignore = true)
    void updateEntityFromDTO(ProduitRequestDTO dto, @MappingTarget Produit entity);
}
```

**Créer tous les mappers**:
1. FournisseurMapper
2. ProduitMapper
3. CommandeMapper
4. StockMapper
5. BonSortieMapper

---

## 💼 PHASE 9: SERVICES (3h)

### Étape 9.1: Créer le Package

```
src/main/java/com/tricol/service/
```

### Étape 9.2: Ordre de Création

1. **FournisseurService** (30 min)
2. **ProduitService** (30 min)
3. **CommandeFournisseurService** (1h)
4. **StockService** ⭐ (1h30) - Le plus important
5. **BonSortieService** (1h)

### Étape 9.3: Exemple - ProduitService

```java
package com.tricol.service;

import com.tricol.dto.request.ProduitRequestDTO;
import com.tricol.dto.response.ProduitResponseDTO;
import com.tricol.entity.Produit;
import com.tricol.exception.*;
import com.tricol.mapper.ProduitMapper;
import com.tricol.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitService {
    
    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;
    
    public List<ProduitResponseDTO> findAll() {
        return produitRepository.findAll()
            .stream()
            .map(produitMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    public ProduitResponseDTO findById(Long id) {
        Produit produit = produitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
        return produitMapper.toResponseDTO(produit);
    }
    
    @Transactional
    public ProduitResponseDTO create(ProduitRequestDTO requestDTO) {
        if (produitRepository.existsByReference(requestDTO.getReference())) {
            throw new BusinessException("Référence déjà existante");
        }
        
        Produit produit = produitMapper.toEntity(requestDTO);
        Produit saved = produitRepository.save(produit);
        return produitMapper.toResponseDTO(saved);
    }
    
    @Transactional
    public ProduitResponseDTO update(Long id, ProduitRequestDTO requestDTO) {
        Produit produit = produitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
        
        produitMapper.updateEntityFromDTO(requestDTO, produit);
        Produit updated = produitRepository.save(produit);
        return produitMapper.toResponseDTO(updated);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit non trouvé");
        }
        produitRepository.deleteById(id);
    }
}
```

📝 **Voir GUIDE_CODE_EXAMPLES.java pour StockService avec algorithme FIFO**

---

## 🎮 PHASE 10: CONTROLLERS (1h)

### Étape 10.1: Créer le Package

```
src/main/java/com/tricol/controller/
```

### Étape 10.2: Créer les Controllers

**Exemple - ProduitController.java**:
```java
package com.tricol.controller;

import com.tricol.dto.request.ProduitRequestDTO;
import com.tricol.dto.response.ProduitResponseDTO;
import com.tricol.service.ProduitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion des produits")
public class ProduitController {
    
    private final ProduitService produitService;
    
    @GetMapping
    @Operation(summary = "Lister tous les produits")
    public ResponseEntity<List<ProduitResponseDTO>> getAll() {
        return ResponseEntity.ok(produitService.findAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID")
    public ResponseEntity<ProduitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.findById(id));
    }
    
    @PostMapping
    @Operation(summary = "Créer un nouveau produit")
    public ResponseEntity<ProduitResponseDTO> create(
            @Valid @RequestBody ProduitRequestDTO request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(produitService.create(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un produit")
    public ResponseEntity<ProduitResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProduitRequestDTO request) {
        return ResponseEntity.ok(produitService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Créer tous les controllers**:
1. FournisseurController
2. ProduitController
3. CommandeFournisseurController
4. StockController
5. BonSortieController

---

## ⚙️ PHASE 11: CONFIGURATION (15 min)

### Étape 11.1: Créer le Package

```
src/main/java/com/tricol/config/
```

### Étape 11.2: OpenApiConfig

```java
package com.tricol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI tricolOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Tricol Stock API")
                .description("API de gestion des commandes et stock FIFO")
                .version("1.0.0"));
    }
}
```

---

## 🧪 PHASE 12: TESTS (1h)

### Étape 12.1: Tester avec Postman

1. Créer un fournisseur
2. Créer un produit
3. Créer une commande
4. Réceptionner la commande
5. Créer un bon de sortie
6. Valider le bon (FIFO)

### Étape 12.2: Vérifier Swagger

```
http://localhost:8080/tricol-stock/swagger-ui.html
```

---

## 📦 PHASE 13: GÉNÉRATION WAR (15 min)

### Étape 13.1: Compiler

```bash
mvn clean package
```

### Étape 13.2: Vérifier le WAR

```
target/tricol-stock-1.0.0.war
```

---

## ✅ CHECKLIST FINALE

- [ ] Toutes les entities créées (8)
- [ ] Tous les enums créés (4)
- [ ] Liquibase fonctionne
- [ ] Tous les repositories créés (8)
- [ ] Tous les DTOs créés (16)
- [ ] Tous les mappers créés (5)
- [ ] Tous les services créés (5)
- [ ] Tous les controllers créés (5)
- [ ] Exceptions gérées
- [ ] Swagger accessible
- [ ] Tests Postman OK
- [ ] WAR généré
- [ ] README.md complet
- [ ] Diagramme UML créé

---

## 🎯 ORDRE RECOMMANDÉ PAR JOUR

### Jour 1 (2h)
- Phase 1: Initialisation
- Phase 2: Enums
- Phase 3: Entities
- Phase 4: Liquibase

### Jour 2 (2h)
- Phase 5: Repositories
- Phase 6: Exceptions
- Phase 7: DTOs

### Jour 3 (2h)
- Phase 8: Mappers
- Phase 9: Services (Fournisseur, Produit)

### Jour 4 (2h)
- Phase 9: Services (Commande, Stock FIFO)

### Jour 5 (2h)
- Phase 9: Services (BonSortie)
- Phase 10: Controllers

### Jour 6 (1h)
- Phase 11: Configuration
- Phase 12: Tests
- Phase 13: WAR

**TOTAL: 11 heures réparties sur 6 jours**
