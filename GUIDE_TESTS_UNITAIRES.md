# Guide Complet - Tests Unitaires et Recherche Avancée

## Table des Matières
1. [Partie 1: Tests Unitaires](#partie-1-tests-unitaires)
2. [Partie 2: Recherche Avancée](#partie-2-recherche-avancée)
3. [Nouveaux Concepts à Maîtriser](#nouveaux-concepts-à-maîtriser)

---

## Partie 1: Tests Unitaires

### Objectif
Tester la logique métier critique du système de gestion de stock, notamment l'algorithme FIFO.

### Concepts Nouveaux à Apprendre

#### 1. JUnit 5
Framework de tests unitaires pour Java.

**Annotations principales:**
- `@Test` - Marque une méthode comme test
- `@BeforeEach` - Exécuté avant chaque test
- `@AfterEach` - Exécuté après chaque test
- `@DisplayName` - Nom descriptif du test
- `@Disabled` - Désactive un test

**Assertions:**
```java
assertEquals(expected, actual);
assertNotNull(object);
assertTrue(condition);
assertThrows(Exception.class, () -> method());
```

#### 2. Mockito
Framework pour créer des objets simulés (mocks).

**Annotations:**
- `@Mock` - Crée un mock
- `@InjectMocks` - Injecte les mocks dans la classe testée
- `@ExtendWith(MockitoExtension.class)` - Active Mockito

**Méthodes principales:**
```java
when(mock.method()).thenReturn(value);
verify(mock).method();
verify(mock, times(2)).method();
```

---

## Tâche 1.1: Tests du Service de Stock et FIFO

### A. Tests de l'Algorithme FIFO

#### Scénario 1: Sortie Partielle d'un Seul Lot

**Contexte:**
- Lot 1: 100 unités à 150 MAD (date: 2025-01-01)
- Sortie demandée: 30 unités

**Résultat attendu:**
- Lot 1: 70 unités restantes
- 1 mouvement de sortie créé
- Stock produit: 70 unités

**Code du test:**
```java
@Test
@DisplayName("Test FIFO: Sortie partielle d'un seul lot")
void testFifoSortiePartielleUnSeulLot() {
    // ARRANGE - Préparation des données
    Produit produit = new Produit();
    produit.setId(1L);
    produit.setStockActuel(100);
    
    LotStock lot = new LotStock();
    lot.setId(1L);
    lot.setQuantiteRestante(100);
    lot.setPrixUnitaire(new BigDecimal("150"));
    lot.setDateEntree(LocalDate.of(2025, 1, 1));
    
    when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
        .thenReturn(List.of(lot));
    
    // ACT - Exécution de la méthode testée
    List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 30);
    
    // ASSERT - Vérification des résultats
    assertEquals(1, mouvements.size());
    assertEquals(30, mouvements.get(0).getQuantite());
    assertEquals(70, lot.getQuantiteRestante());
    assertEquals(TypeMouvement.SORTIE, mouvements.get(0).getTypeMouvement());
}
```

#### Scénario 2: Sortie Consommant Plusieurs Lots

**Contexte:**
- Lot 1: 50 unités à 150 MAD (date: 2025-01-01)
- Lot 2: 80 unités à 160 MAD (date: 2025-01-15)
- Sortie demandée: 100 unités

**Résultat attendu:**
- Lot 1: 0 unités (épuisé)
- Lot 2: 30 unités restantes
- 2 mouvements de sortie créés

**Code du test:**
```java
@Test
@DisplayName("Test FIFO: Sortie consommant plusieurs lots")
void testFifoSortiePlusieursLots() {
    // ARRANGE
    Produit produit = new Produit();
    produit.setId(1L);
    
    LotStock lot1 = new LotStock();
    lot1.setQuantiteRestante(50);
    lot1.setPrixUnitaire(new BigDecimal("150"));
    lot1.setDateEntree(LocalDate.of(2025, 1, 1));
    
    LotStock lot2 = new LotStock();
    lot2.setQuantiteRestante(80);
    lot2.setPrixUnitaire(new BigDecimal("160"));
    lot2.setDateEntree(LocalDate.of(2025, 1, 15));
    
    when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
        .thenReturn(List.of(lot1, lot2));
    
    // ACT
    List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 100);
    
    // ASSERT
    assertEquals(2, mouvements.size());
    assertEquals(50, mouvements.get(0).getQuantite()); // Lot 1 épuisé
    assertEquals(50, mouvements.get(1).getQuantite()); // 50 du Lot 2
    assertEquals(0, lot1.getQuantiteRestante());
    assertEquals(30, lot2.getQuantiteRestante());
}
```

#### Scénario 3: Stock Insuffisant

**Contexte:**
- Lot 1: 30 unités
- Sortie demandée: 50 unités

**Résultat attendu:**
- Exception `InsufficientStockException`

**Code du test:**
```java
@Test
@DisplayName("Test FIFO: Stock insuffisant - Exception attendue")
void testFifoStockInsuffisant() {
    // ARRANGE
    Produit produit = new Produit();
    produit.setId(1L);
    produit.setNom("Produit Test");
    
    LotStock lot = new LotStock();
    lot.setQuantiteRestante(30);
    
    when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
        .thenReturn(List.of(lot));
    
    // ACT & ASSERT
    InsufficientStockException exception = assertThrows(
        InsufficientStockException.class,
        () -> fifoStockStrategy.consumeStock(produit, 50)
    );
    
    assertTrue(exception.getMessage().contains("Stock insuffisant"));
}
```

#### Scénario 4: Épuisement Exact du Stock

**Contexte:**
- Lot 1: 50 unités
- Lot 2: 50 unités
- Sortie demandée: 100 unités

**Résultat attendu:**
- Les 2 lots à 0
- 2 mouvements créés

**Code du test:**
```java
@Test
@DisplayName("Test FIFO: Épuisement exact du stock")
void testFifoEpuisementExact() {
    // ARRANGE
    Produit produit = new Produit();
    
    LotStock lot1 = new LotStock();
    lot1.setQuantiteRestante(50);
    
    LotStock lot2 = new LotStock();
    lot2.setQuantiteRestante(50);
    
    when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
        .thenReturn(List.of(lot1, lot2));
    
    // ACT
    List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 100);
    
    // ASSERT
    assertEquals(2, mouvements.size());
    assertEquals(0, lot1.getQuantiteRestante());
    assertEquals(0, lot2.getQuantiteRestante());
}
```

---

### B. Tests de Création Automatique de Lot

**Objectif:** Vérifier qu'une réception crée automatiquement un lot traçable.

**Code du test:**
```java
@Test
@DisplayName("Test: Création automatique de lot lors de la réception")
void testCreationAutomatiqueLot() {
    // ARRANGE
    Commande commande = new Commande();
    commande.setId(1L);
    commande.setStatut(StatutCommande.VALIDEE);
    
    Produit produit = new Produit();
    produit.setId(1L);
    produit.setStockActuel(0);
    
    LigneCommande ligne = new LigneCommande();
    ligne.setProduit(produit);
    ligne.setQuantite(100);
    ligne.setPrixUnitaire(new BigDecimal("150"));
    
    commande.setLignes(List.of(ligne));
    
    ReceptionCreateRequest request = new ReceptionCreateRequest();
    request.setCommandeId(1L);
    request.setDateReception(LocalDate.now());
    
    when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
    when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
    
    // ACT
    receptionService.create(request);
    
    // ASSERT
    ArgumentCaptor<LotStock> lotCaptor = ArgumentCaptor.forClass(LotStock.class);
    verify(lotStockRepository).save(lotCaptor.capture());
    
    LotStock lotCree = lotCaptor.getValue();
    assertNotNull(lotCree.getNumeroLot());
    assertEquals(100, lotCree.getQuantiteInitiale());
    assertEquals(100, lotCree.getQuantiteRestante());
    assertEquals(new BigDecimal("150"), lotCree.getPrixUnitaire());
    assertNotNull(lotCree.getDateEntree());
}
```

---

### C. Tests de Valorisation du Stock

**Objectif:** Tester le calcul de la valeur totale du stock selon FIFO.

**Code du test:**
```java
@Test
@DisplayName("Test: Calcul de valorisation du stock avec plusieurs lots")
void testValorisationStockPlusieursLots() {
    // ARRANGE
    Produit produit = new Produit();
    produit.setId(1L);
    
    LotStock lot1 = new LotStock();
    lot1.setQuantiteRestante(50);
    lot1.setPrixUnitaire(new BigDecimal("150")); // 50 × 150 = 7500
    
    LotStock lot2 = new LotStock();
    lot2.setQuantiteRestante(30);
    lot2.setPrixUnitaire(new BigDecimal("160")); // 30 × 160 = 4800
    
    when(lotStockRepository.findByProduitId(1L))
        .thenReturn(List.of(lot1, lot2));
    
    // ACT
    BigDecimal valorisation = stockService.calculerValorisationStock(1L);
    
    // ASSERT
    assertEquals(new BigDecimal("12300"), valorisation); // 7500 + 4800
}
```

---

## Tâche 1.2: Tests des Transitions de Statut

**Objectif:** Vérifier que la validation d'un bon de sortie déclenche les actions automatiques.

**Code du test:**
```java
@Test
@DisplayName("Test: Validation bon de sortie déclenche création mouvements")
void testValidationBonSortieCreeeMouvements() {
    // ARRANGE
    BonSortie bonSortie = new BonSortie();
    bonSortie.setId(1L);
    bonSortie.setStatut(StatutBonSortie.BROUILLON);
    
    Produit produit = new Produit();
    produit.setId(1L);
    produit.setStockActuel(100);
    
    LigneBonSortie ligne = new LigneBonSortie();
    ligne.setProduit(produit);
    ligne.setQuantite(30);
    
    bonSortie.setLignes(List.of(ligne));
    
    LotStock lot = new LotStock();
    lot.setQuantiteRestante(100);
    lot.setPrixUnitaire(new BigDecimal("150"));
    
    when(bonSortieRepository.findById(1L)).thenReturn(Optional.of(bonSortie));
    when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
        .thenReturn(List.of(lot));
    
    // ACT
    bonSortieService.valider(1L);
    
    // ASSERT
    assertEquals(StatutBonSortie.VALIDE, bonSortie.getStatut());
    assertNotNull(bonSortie.getDateValidation());
    verify(mouvementStockRepository, times(1)).save(any(MouvementStock.class));
    assertEquals(70, lot.getQuantiteRestante());
    assertEquals(70, produit.getStockActuel());
}
```

---

## Structure du Projet de Tests

```
src/test/java/com/tricol/stock/
├── service/
│   ├── FifoStockStrategyTest.java
│   ├── StockServiceTest.java
│   ├── ReceptionServiceTest.java
│   └── BonSortieServiceTest.java
```

---

## Configuration Maven pour les Tests

Ajoutez dans `pom.xml`:

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Commandes pour Exécuter les Tests

```bash
# Exécuter tous les tests
mvn test

# Exécuter une classe de test spécifique
mvn test -Dtest=FifoStockStrategyTest

# Exécuter avec rapport de couverture
mvn test jacoco:report
```

---

## Checklist Partie 1

- [ ] Test FIFO: Sortie partielle d'un lot
- [ ] Test FIFO: Sortie de plusieurs lots
- [ ] Test FIFO: Stock insuffisant
- [ ] Test FIFO: Épuisement exact
- [ ] Test: Création automatique de lot
- [ ] Test: Valorisation du stock
- [ ] Test: Validation bon de sortie
- [ ] Tous les tests passent (vert)
- [ ] Couverture de code > 80%
