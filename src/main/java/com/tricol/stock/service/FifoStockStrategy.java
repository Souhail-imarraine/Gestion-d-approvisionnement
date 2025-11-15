package com.tricol.stock.service;

import com.tricol.stock.entity.LotStock;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.exception.InsufficientStockException;
import com.tricol.stock.repository.LotStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FifoStockStrategy {
    
    private final LotStockRepository lotStockRepository;
    
    public List<MouvementStock> consumeStock(Produit produit, Integer quantity) {
        List<MouvementStock> mouvements = new ArrayList<>();
        Integer remaining = quantity;
        
        List<LotStock> lots = lotStockRepository
            .findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(produit.getId(), 0);
        
        for (LotStock lot : lots) {
            if (remaining <= 0) break;
            
            Integer fromThisLot = Math.min(lot.getQuantiteRestante(), remaining);

            MouvementStock mouvement = new MouvementStock();
            mouvement.setProduit(produit);
            mouvement.setLot(lot);
            mouvement.setTypeMouvement(TypeMouvement.SORTIE);
            mouvement.setQuantite(fromThisLot);
            mouvement.setPrixUnitaire(lot.getPrixUnitaire());
            mouvement.setDateMouvement(LocalDate.now());
            
            mouvements.add(mouvement);

            lot.setQuantiteRestante(lot.getQuantiteRestante() - fromThisLot);
            remaining -= fromThisLot;
        }
        
        if (remaining > 0) {
            throw new InsufficientStockException("Stock insuffisant pour " + produit.getNom());
        }
        
        return mouvements;
    }
}