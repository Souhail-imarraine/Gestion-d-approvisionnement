package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String raisonSociale;

    @Column(length = 200)
    private String adresse;

    @Column(length = 50)
    private String ville;

    @Column(length = 100)
    private String personneContact;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(unique = true, length = 15)
    private String ice;

}
