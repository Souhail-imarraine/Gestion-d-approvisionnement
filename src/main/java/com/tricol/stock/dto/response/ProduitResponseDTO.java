package com.tricol.stock.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitResponseDTO {

    private Long id;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prixUnitaire;
    private String categorie;
    private Integer stockActuel;
    private Integer pointCommande;
    private String uniteMesure;
}
