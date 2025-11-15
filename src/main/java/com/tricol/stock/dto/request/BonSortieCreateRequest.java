package com.tricol.stock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BonSortieCreateRequest {
    
    @NotBlank(message = "L'atelier est obligatoire")
    private String atelier;

    private String commentaire;
    
    @NotEmpty(message = "Le bon doit contenir au moins une ligne")
    private List<LigneBonSortieCreateRequest> lignes;
}
