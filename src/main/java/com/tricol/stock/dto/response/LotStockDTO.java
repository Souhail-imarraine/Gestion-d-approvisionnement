package com.tricol.stock.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotStockDTO {
    
    private Long id;
    private String numeroLot;
    private LocalDate dateEntree;
    private Integer quantiteInitiale;
    private Integer quantiteRestante;
    private BigDecimal prixUnitaire;
    private Long produitId;
    private String produitNom;
    private Long commandeId;
    private String commandeNumero;
}
