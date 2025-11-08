package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lots_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LotStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_lot", unique = true, nullable = false, length = 50)
    private String numeroLot;
    
    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree;
    
    @Column(name = "quantite_initiale", nullable = false)
    private Integer quantiteInitiale;
    
    @Column(name = "quantite_restante", nullable = false)
    private Integer quantiteRestante;
    
    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;
    
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;
}
