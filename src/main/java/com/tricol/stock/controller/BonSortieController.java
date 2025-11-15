package com.tricol.stock.controller;

import com.tricol.stock.dto.request.BonSortieCreateRequest;
import com.tricol.stock.dto.request.BonSortieUpdateRequest;
import com.tricol.stock.dto.response.BonSortieResponseDTO;
import com.tricol.stock.service.BonSortieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bons-sortie")
@RequiredArgsConstructor
public class BonSortieController {
    
    private final BonSortieService bonSortieService;
    
    @GetMapping
    public ResponseEntity<List<BonSortieResponseDTO>> findAll() {
        return ResponseEntity.ok(bonSortieService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BonSortieResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bonSortieService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<BonSortieResponseDTO> create(@Valid @RequestBody BonSortieCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bonSortieService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BonSortieResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BonSortieUpdateRequest dto) {
        return ResponseEntity.ok(bonSortieService.update(id, dto));
    }
    
    @PutMapping("/{id}/valider")
    public ResponseEntity<BonSortieResponseDTO> valider(@PathVariable Long id) {
        return ResponseEntity.ok(bonSortieService.valider(id));
    }
    
    @PutMapping("/{id}/annuler")
    public ResponseEntity<HashMap<String, String>> annuler(@PathVariable Long id) {
        bonSortieService.annuler(id);
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Bon de sortie annulé avec succès");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/atelier/{atelier}")
    public ResponseEntity<List<BonSortieResponseDTO>> findByAtelier(@PathVariable String atelier) {
        return ResponseEntity.ok(bonSortieService.findByAtelier(atelier));
    }
}
