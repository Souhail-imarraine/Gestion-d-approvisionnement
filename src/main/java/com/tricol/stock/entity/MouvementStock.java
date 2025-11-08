package com.tricol.stock.entity;

import com.tricol.stock.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mouvements_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date_mouvement", nullable = false)
    private LocalDate dateMouvement;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 20)
    private TypeMouvement typeMouvement;
    
    @Column(name = "quantite", nullable = false)
    private Integer quantite;
    
    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;
    
    @Column(name = "reference", length = 100)
    private String reference;
    
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    private LotStock lot;
}
