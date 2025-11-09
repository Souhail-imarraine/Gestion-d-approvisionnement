package com.tricol.stock.dto;

import com.tricol.stock.enums.StatutCommande;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeDTO {
    private Long id;

    private String numero;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate dateCommande;
    
    @NotNull(message = "La date de livraison prévue est obligatoire")
    private LocalDate dateLivraisonPrevue;
    
    private StatutCommande statut;
    
    private BigDecimal montantTotal;
    
    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;
    
    private String fournisseurNom;
    
    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    private List<LigneCommandeDTO> lignes;
}
