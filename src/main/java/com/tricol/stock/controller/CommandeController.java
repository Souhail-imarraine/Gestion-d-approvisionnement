package com.tricol.stock.controller;

import com.tricol.stock.dto.CommandeDTO;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.service.CommandeService;
import com.tricol.stock.service.ReceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
public class CommandeController {
    
    private final CommandeService service;
    private final ReceptionService receptionService;
    
    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<CommandeDTO> create(@Valid @RequestBody CommandeDTO dto) {
        CommandeDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CommandeDTO> update(@PathVariable Long id, @Valid @RequestBody CommandeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CommandeDTO>> getByStatut(@PathVariable StatutCommande statut) {
        return ResponseEntity.ok(service.findByStatut(statut));
    }
    
    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<CommandeDTO>> getByFournisseur(@PathVariable Long fournisseurId) {
        return ResponseEntity.ok(service.findByFournisseur(fournisseurId));
    }
    
    @PatchMapping("/{id}/statut")
    public ResponseEntity<CommandeDTO> changerStatut(@PathVariable Long id, @RequestParam StatutCommande statut) {
        return ResponseEntity.ok(service.changerStatut(id, statut));
    }
    
    @PutMapping("/{id}/reception")
    public ResponseEntity<CommandeDTO> receptionner(@PathVariable Long id) {
        return ResponseEntity.ok(receptionService.receptionnerCommande(id));
    }
}
