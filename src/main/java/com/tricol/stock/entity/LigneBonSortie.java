package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_bon_sortie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonSortie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer quantite;
    
    @ManyToOne
    @JoinColumn(name = "bon_sortie_id", nullable = false)
    private BonSortie bonSortie;
    
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
}