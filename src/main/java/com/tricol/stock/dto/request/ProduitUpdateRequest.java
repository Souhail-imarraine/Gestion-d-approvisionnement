package com.tricol.stock.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitUpdateRequest {

    @Size(max = 50, message = "La référence ne peut pas dépasser 50 caractères")
    private String reference;

    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private BigDecimal prixUnitaire;

    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categorie;

    @Min(value = 0, message = "Le point de commande ne peut pas être négatif")
    private Integer pointCommande;

    @Size(max = 20, message = "L'unité de mesure ne peut pas dépasser 20 caractères")
    private String uniteMesure;
}
