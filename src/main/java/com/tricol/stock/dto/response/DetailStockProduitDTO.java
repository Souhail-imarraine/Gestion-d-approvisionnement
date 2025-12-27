package com.tricol.stock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DetailStockProduitDTO {
    private Long produitId;
    private String produitNom;
    private String produitReference;
    private Integer stockActuel;
    private Integer pointCommande;
    private BigDecimal valeurTotale;
    private List<LotStockDTO> lots;
}
