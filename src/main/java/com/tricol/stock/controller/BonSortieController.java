package com.tricol.stock.controller;

import com.tricol.stock.dto.BonSortieDTO;
import com.tricol.stock.service.BonSortieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bons-sortie")
@RequiredArgsConstructor
public class BonSortieController {
    
    private final BonSortieService bonSortieService;
    
    @GetMapping
    public ResponseEntity<List<BonSortieDTO>> findAll() {
        return ResponseEntity.ok(bonSortieService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BonSortieDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bonSortieService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<BonSortieDTO> create(@RequestBody BonSortieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bonSortieService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BonSortieDTO> update(@PathVariable Long id, @RequestBody BonSortieDTO dto) {
        return ResponseEntity.ok(bonSortieService.update(id, dto));
    }
    
    @PutMapping("/{id}/valider")
    public ResponseEntity<BonSortieDTO> valider(@PathVariable Long id) {
        return ResponseEntity.ok(bonSortieService.valider(id));
    }
    
    @PutMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        bonSortieService.annuler(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/atelier/{atelier}")
    public ResponseEntity<List<BonSortieDTO>> findByAtelier(@PathVariable String atelier) {
        return ResponseEntity.ok(bonSortieService.findByAtelier(atelier));
    }
}