package com.tricol.stock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LigneBonSortieCreateRequest {
    
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
}
