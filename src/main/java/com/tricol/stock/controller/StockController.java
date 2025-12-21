package com.tricol.stock.controller;

import com.tricol.stock.dto.response.*;
import com.tricol.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockService stockService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<List<EtatStockDTO>> getEtatGlobalStock() {
        return ResponseEntity.ok(stockService.getEtatGlobalStock());
    }
    
    @GetMapping("/produit/{id}")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<DetailStockProduitDTO> getDetailStockProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getDetailStockProduit(id));
    }
    
    @GetMapping("/mouvements")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<List<MouvementStockDTO>> getHistoriqueMouvements() {
        return ResponseEntity.ok(stockService.getHistoriqueMouvements());
    }
    
    @GetMapping("/mouvements/produit/{id}")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsProduit(id));
    }
    
    @GetMapping("/alertes")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<List<EtatStockDTO>> getProduitsEnAlerte() {
        return ResponseEntity.ok(stockService.getProduitsEnAlerte());
    }
    
    @GetMapping("/valorisation")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<ValorisationStockDTO> getValorisationStock() {
        return ResponseEntity.ok(stockService.getValorisationStock());
    }

}
