package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String reference;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double prixUnitaire;
    
    @Column(length = 50)
    private String categorie;
    
    @Column(nullable = false)
    private Integer stockActuel = 0;
    
    @Column(nullable = false)
    private Integer pointCommande = 10;
    
    @Column(length = 20)
    private String uniteMesure;
}
