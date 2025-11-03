package com.tricol.stock.repository;

import com.tricol.stock.entity.Commande;
import com.tricol.stock.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    
    Optional<Commande> findByNumero(String numero);
    
    List<Commande> findByStatut(StatutCommande statut);
    
    List<Commande> findByFournisseurId(Long fournisseurId);
    
    @Query("SELECT c FROM Commande c WHERE c.statut = com.tricol.stock.enums.StatutCommande.EN_ATTENTE OR c.statut = com.tricol.stock.enums.StatutCommande.VALIDEE")
    List<Commande> findCommandesEnCours();
}
