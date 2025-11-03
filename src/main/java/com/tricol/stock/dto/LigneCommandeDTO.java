package com.tricol.stock.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeDTO {
    
    private Long id;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private BigDecimal prixUnitaire;
    
    private BigDecimal sousTotal;
    
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    
    private String produitNom;
}
