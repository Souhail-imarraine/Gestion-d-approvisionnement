package com.tricol.stock.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurResponseDTO {

    private Long id;
    private String raisonSociale;
    private String adresse;
    private String ville;
    private String personneContact;
    private String email;
    private String telephone;
    private String ice;
}
