package com.tricol.stock.controller;

import com.tricol.stock.dto.*;
import com.tricol.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockService stockService;
    
    @GetMapping
    public ResponseEntity<List<EtatStockDTO>> getEtatGlobalStock() {
        return ResponseEntity.ok(stockService.getEtatGlobalStock());
    }
    
    @GetMapping("/produit/{id}")
    public ResponseEntity<DetailStockProduitDTO> getDetailStockProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getDetailStockProduit(id));
    }
    
    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockDTO>> getHistoriqueMouvements() {
        return ResponseEntity.ok(stockService.getHistoriqueMouvements());
    }
    
    @GetMapping("/mouvements/produit/{id}")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsProduit(id));
    }
    
    @GetMapping("/alertes")
    public ResponseEntity<List<EtatStockDTO>> getProduitsEnAlerte() {
        return ResponseEntity.ok(stockService.getProduitsEnAlerte());
    }
    
    @GetMapping("/valorisation")
    public ResponseEntity<ValorisationStockDTO> getValorisationStock() {
        return ResponseEntity.ok(stockService.getValorisationStock());
    }
}
