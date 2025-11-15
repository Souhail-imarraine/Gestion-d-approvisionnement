package com.tricol.stock.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BonSortieUpdateRequest {
    
    private String atelier;
    
    private String commentaire;
    
    private List<LigneBonSortieCreateRequest> lignes;
}
