package com.tricol.stock.controller;

import com.tricol.stock.dto.request.ProduitCreateRequest;
import com.tricol.stock.dto.request.ProduitUpdateRequest;
import com.tricol.stock.dto.response.ProduitResponseDTO;
import com.tricol.stock.dto.response.StockDTO;
import com.tricol.stock.service.impl.ProduitServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitServiceImp service;

    @GetMapping
    public ResponseEntity<List<ProduitResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProduitResponseDTO> create(@Valid @RequestBody ProduitCreateRequest dto) {
        ProduitResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProduitUpdateRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        HashMap<String, String> message = new HashMap<>();
        message.put("message", "Produit supprime avec succes");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<ProduitResponseDTO>> getProduitsEnAlerte() {
        return ResponseEntity.ok(service.findProduitsEnAlerte());
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<StockDTO> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStock(id));
    }
}
