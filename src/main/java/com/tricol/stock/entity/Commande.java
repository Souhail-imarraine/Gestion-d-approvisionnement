package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tricol.stock.enums.StatutCommande;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false, length = 50)
    private String numero;
    
    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;
    
    @Column(name = "date_livraison_prevue", nullable = false)
    private LocalDate dateLivraisonPrevue;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;
    
    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;
    
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @PrePersist
    public void onCreate(){
        if (this.dateCommande == null){
            this.dateCommande = LocalDate.now();
        }
    }
}
