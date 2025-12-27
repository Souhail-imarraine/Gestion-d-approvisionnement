package com.tricol.stock.dto.response;

import com.tricol.stock.enums.StatutCommande;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeResponseDTO {
    private Long id;
    private String numero;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonPrevue;
    private StatutCommande statut;
    private BigDecimal montantTotal;
    private Long fournisseurId;
    private String fournisseurNom;
    private List<LigneCommandeResponseDTO> lignes;
}
