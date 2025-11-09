package com.tricol.stock.entity;

import com.tricol.stock.enums.StatutBonSortie;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_sortie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BonSortie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numero;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutBonSortie statut = StatutBonSortie.BROUILLON;
    
    @Column(length = 100)
    private String atelier;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @OneToMany(mappedBy = "bonSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneBonSortie> lignes = new ArrayList<>();
}