package com.tricol.stock.dto.response;

import com.tricol.stock.enums.TypeMouvement;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockDTO {
    
    private Long id;
    private LocalDate dateMouvement;
    private TypeMouvement typeMouvement;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private String reference;
    private Long produitId;
    private String produitNom;
    private Long lotId;
    private String lotNumero;
}
