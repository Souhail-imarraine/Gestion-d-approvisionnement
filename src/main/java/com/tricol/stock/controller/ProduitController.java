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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitServiceImp service;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_PRODUIT')")
    public ResponseEntity<List<ProduitResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_PRODUIT')")
    public ResponseEntity<ProduitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUIT')")
    public ResponseEntity<ProduitResponseDTO> create(@Valid @RequestBody ProduitCreateRequest dto) {
        ProduitResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUIT')")
    public ResponseEntity<ProduitResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProduitUpdateRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PRODUIT')")
    public ResponseEntity<HashMap<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        HashMap<String, String> message = new HashMap<>();
        message.put("message", "Produit supprime avec succes");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/alertes")
    @PreAuthorize("hasAuthority('READ_PRODUIT')")
    public ResponseEntity<List<ProduitResponseDTO>> getProduitsEnAlerte() {
        return ResponseEntity.ok(service.findProduitsEnAlerte());
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('READ_STOCK')")
    public ResponseEntity<StockDTO> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStock(id));
    }
}
