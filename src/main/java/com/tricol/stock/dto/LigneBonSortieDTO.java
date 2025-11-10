package com.tricol.stock.dto;

import lombok.Data;

@Data
public class LigneBonSortieDTO {
    private Long id;
    private Integer quantite;
    private Long produitId;
    private String produitNom;
    private String produitReference;
}