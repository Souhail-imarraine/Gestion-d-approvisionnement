package com.tricol.stock.repository;

import com.tricol.stock.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByIce(String ice);

    List<Fournisseur> findByVille(String ville);
    List<Fournisseur> findByRaisonSocialeContainingIgnoreCase(String nom);
}
