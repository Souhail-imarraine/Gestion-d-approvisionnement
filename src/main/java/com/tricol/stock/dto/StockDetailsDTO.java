package com.tricol.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StockDetailsDTO {
    private Long produitId;
    private String reference;
    private String nom;
    private Integer stockActuel;
    private Integer pointCommande;
    private boolean enAlerte;
    private List<LotInfo> lots;
    private BigDecimal valorisationTotale;
    
    @Data
    public static class LotInfo {
        private String numeroLot;
        private LocalDate dateEntree;
        private Integer quantiteRestante;
        private BigDecimal prixUnitaire;
        private BigDecimal valorisation;
    }
}