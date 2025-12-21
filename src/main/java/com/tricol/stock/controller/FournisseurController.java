package com.tricol.stock.controller;

import com.tricol.stock.dto.request.FournisseurCreateRequest;
import com.tricol.stock.dto.request.FournisseurUpdateRequest;
import com.tricol.stock.dto.response.FournisseurResponseDTO;
import com.tricol.stock.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService FournisseurService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FOURNISSEUR')")
    public ResponseEntity<List<FournisseurResponseDTO>> getAll() {
        return ResponseEntity.ok(FournisseurService.findAll());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('READ_FOURNISSEUR')")
    public ResponseEntity<List<FournisseurResponseDTO>> searchByName(@RequestParam String name){
        return ResponseEntity.ok(FournisseurService.searchByName(name));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FOURNISSEUR')")
    public ResponseEntity<FournisseurResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(FournisseurService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_FOURNISSEUR')")
    public ResponseEntity<FournisseurResponseDTO> create(@Valid @RequestBody FournisseurCreateRequest dto) {
        FournisseurResponseDTO created = FournisseurService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_FOURNISSEUR')")
    public ResponseEntity<FournisseurResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurUpdateRequest dto) {
        return ResponseEntity.ok(FournisseurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_FOURNISSEUR')")
    public ResponseEntity<HashMap<String, String>> delete(@PathVariable Long id) {
        FournisseurService.delete(id);
        HashMap<String, String> response  = new HashMap<>();
        response.put("message", "fournisseur supprime avec succes");
        return ResponseEntity.ok(response);
    }

}
