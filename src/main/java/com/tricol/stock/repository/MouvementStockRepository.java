package com.tricol.stock.repository;

import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.enums.TypeMouvement;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    
    List<MouvementStock> findByProduitId(Long produitId);
    
    List<MouvementStock> findByLotId(Long lotId);
    
    List<MouvementStock> findByTypeMouvement(TypeMouvement type);
    
    List<MouvementStock> findByProduitIdOrderByDateMouvementDesc(Long produitId);
    
    List<MouvementStock> findAllByOrderByDateMouvementDesc();

    @Query("SELECT m FROM MouvementStock m where m.typeMouvement = :typeMouvement AND m.dateMouvement >= :startDate AND m.dateMouvement <= :endDate")
    List<MouvementStock> filtrageAvecType(@Param("typeMouvement") String typeMouvement, @Param("startDate")LocalDate startDate, @Param("endDate") LocalDate endDate);

}
