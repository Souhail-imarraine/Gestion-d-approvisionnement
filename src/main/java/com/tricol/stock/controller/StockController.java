package com.tricol.stock.controller;

import com.tricol.stock.dto.MouvementStockDTO;
import com.tricol.stock.dto.StockDTO;
import com.tricol.stock.dto.StockDetailsDTO;
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
    public ResponseEntity<List<StockDTO>> getStockOverview() {
        return ResponseEntity.ok(stockService.getStockOverview());
    }
    
    @GetMapping("/produit/{id}")
    public ResponseEntity<StockDetailsDTO> getStockDetails(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockDetails(id));
    }
    
    @GetMapping("/alertes")
    public ResponseEntity<List<StockDTO>> getStockAlertes() {
        return ResponseEntity.ok(stockService.getStockAlertes());
    }
    
    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockDTO>> getAllMouvements() {
        return ResponseEntity.ok(stockService.getAllMouvements());
    }
    
    @GetMapping("/mouvements/produit/{id}")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsByProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvements(id));
    }
}