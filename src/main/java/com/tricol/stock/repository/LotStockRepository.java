package com.tricol.stock.repository;

import com.tricol.stock.entity.LotStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotStockRepository extends JpaRepository<LotStock, Long> {
    
    List<LotStock> findByProduitId(Long produitId);
    
    List<LotStock> findByCommandeId(Long commandeId);

    List<LotStock> findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(Long produitId, Integer quantite);
}
