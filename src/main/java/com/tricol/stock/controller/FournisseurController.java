package com.tricol.stock.controller;

import com.tricol.stock.dto.FournisseurDTO;
import com.tricol.stock.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService FournisseurService;

    @GetMapping
    public ResponseEntity<List<FournisseurDTO>> getAll() {
        return ResponseEntity.ok(FournisseurService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FournisseurDTO>> searchByName(@RequestParam String name){
        return ResponseEntity.ok(FournisseurService.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(FournisseurService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FournisseurDTO> create(@Valid @RequestBody FournisseurDTO dto) {
        FournisseurDTO created = FournisseurService.create(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurDTO dto) {
        return ResponseEntity.ok(FournisseurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, String>> delete(@PathVariable Long id) {
        FournisseurService.delete(id);
        HashMap<String, String> response  = new HashMap<>();
        response.put("message", "fournisseur supprime avec succes");
        return ResponseEntity.ok(response);
    }

}
