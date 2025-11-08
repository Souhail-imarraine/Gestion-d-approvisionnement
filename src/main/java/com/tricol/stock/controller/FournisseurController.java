package com.tricol.stock.controller;

import com.tricol.stock.dto.FournisseurDTO;
import com.tricol.stock.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService service;

    @GetMapping
    public ResponseEntity<List<FournisseurDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FournisseurDTO>> searchByName(@RequestParam String name){
        return ResponseEntity.ok(service.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<FournisseurDTO> create(@Valid @RequestBody FournisseurDTO dto) {
        FournisseurDTO created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
