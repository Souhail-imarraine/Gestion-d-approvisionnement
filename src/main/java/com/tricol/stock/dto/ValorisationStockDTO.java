package com.tricol.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ValorisationStockDTO {
    private BigDecimal valeurTotale;
    private Integer nombreProduits;
    private Integer quantiteTotale;
}
