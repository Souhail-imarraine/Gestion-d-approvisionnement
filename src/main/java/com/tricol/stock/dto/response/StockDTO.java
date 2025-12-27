package com.tricol.stock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockDTO {
    private Long produitId;
    private String produitNom;
    private Integer stockActuel;
    private Integer pointCommande;
    private String uniteMesure;
    private Boolean enAlerte;
}
