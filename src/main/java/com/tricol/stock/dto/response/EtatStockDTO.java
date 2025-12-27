package com.tricol.stock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EtatStockDTO {
    private Long produitId;
    private String produitNom;
    private String produitReference;
    private Integer stockActuel;
    private Integer pointCommande;
    private Boolean enAlerte;
    private BigDecimal valeurStock;
}
