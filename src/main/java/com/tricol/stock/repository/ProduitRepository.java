package com.tricol.stock.repository;

import com.tricol.stock.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    Optional<Produit> findByReference(String reference);
    boolean existsByreference(String reference);
    List<Produit> findByCategorie(String categorie);

    @Query("SELECT p FROM Produit p WHERE p.stockActuel < p.pointCommande")
    List<Produit> findProduitsEnAlerte();
}
