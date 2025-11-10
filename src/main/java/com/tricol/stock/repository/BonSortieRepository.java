package com.tricol.stock.repository;

import com.tricol.stock.entity.BonSortie;
import com.tricol.stock.enums.StatutBonSortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonSortieRepository extends JpaRepository<BonSortie, Long> {
    
    List<BonSortie> findByStatut(StatutBonSortie statut);
    
    List<BonSortie> findByAtelier(String atelier);
    
    boolean existsByNumero(String numero);
}