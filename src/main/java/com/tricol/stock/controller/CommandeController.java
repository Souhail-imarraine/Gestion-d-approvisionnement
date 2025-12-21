package com.tricol.stock.controller;

import com.tricol.stock.dto.request.CommandeCreateRequest;
import com.tricol.stock.dto.request.CommandeUpdateRequest;
import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.service.CommandeService;
import com.tricol.stock.service.ReceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
public class CommandeController {
    
    private final CommandeService service;
    private final ReceptionService receptionService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('READ_COMMANDE')")
    public ResponseEntity<List<CommandeResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_COMMANDE')")
    public ResponseEntity<CommandeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_COMMANDE')")
    public ResponseEntity<CommandeResponseDTO> create(@Valid @RequestBody CommandeCreateRequest dto) {
        CommandeResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_COMMANDE')")
    public ResponseEntity<CommandeResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CommandeUpdateRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_COMMANDE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAuthority('READ_COMMANDE')")
    public ResponseEntity<List<CommandeResponseDTO>> getByStatut(@PathVariable StatutCommande statut) {
        return ResponseEntity.ok(service.findByStatut(statut));
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    @PreAuthorize("hasAuthority('READ_COMMANDE')")
    public ResponseEntity<List<CommandeResponseDTO>> getByFournisseur(@PathVariable Long fournisseurId) {
        return ResponseEntity.ok(service.findByFournisseur(fournisseurId));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAuthority('UPDATE_COMMANDE')")
    public ResponseEntity<CommandeResponseDTO> changerStatut(@PathVariable Long id, @RequestParam String statut) {
        return ResponseEntity.ok(service.changerStatut(id, statut));
    }

    @PutMapping("/{id}/reception")
    @PreAuthorize("hasAuthority('RECEPTION_COMMANDE')")
    public ResponseEntity<CommandeResponseDTO> receptionner(@PathVariable Long id) {
        return ResponseEntity.ok(receptionService.receptionnerCommande(id));
    }
}
