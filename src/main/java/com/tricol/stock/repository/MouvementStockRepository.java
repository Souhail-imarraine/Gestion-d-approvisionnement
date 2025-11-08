package com.tricol.stock.repository;

import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.enums.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    
    List<MouvementStock> findByProduitId(Long produitId);
    
    List<MouvementStock> findByLotId(Long lotId);
    
    List<MouvementStock> findByTypeMouvement(TypeMouvement type);
    
    List<MouvementStock> findByProduitIdOrderByDateMouvementDesc(Long produitId);
    
    List<MouvementStock> findAllByOrderByDateMouvementDesc();
}
