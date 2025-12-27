package com.tricol.stock.dto.response;

import lombok.Data;

@Data
public class LigneBonSortieResponseDTO {
    private Long id;
    private Integer quantite;
    private Long produitId;
    private String produitNom;
    private String produitReference;
}
