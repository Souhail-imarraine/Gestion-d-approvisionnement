package com.tricol.stock.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurUpdateRequest {

    @Size(min = 3, max = 100, message = "La raison sociale doit contenir entre 3 et 100 caractères")
    private String raisonSociale;

    @Size(max = 250, message = "L'adresse ne peut pas dépasser 250 caractères")
    private String adresse;

    @Size(max = 50, message = "La ville ne peut pas dépasser 50 caractères")
    private String ville;

    @Size(max = 100, message = "Le nom de contact ne peut pas dépasser 100 caractères")
    private String personneContact;

    @Email(message = "Email invalide")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Le téléphone doit contenir 10 chiffres")
    private String telephone;

    @Pattern(regexp = "^[0-9]{15}$", message = "L'ICE doit contenir 15 chiffres")
    private String ice;
}
