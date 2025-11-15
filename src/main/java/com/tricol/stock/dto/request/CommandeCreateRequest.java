
package com.tricol.stock.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeCreateRequest {

    @NotNull(message = "La date de livraison prévue est obligatoire")
    @FutureOrPresent
    private LocalDate dateLivraisonPrevue;

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<LigneCommandeCreateRequest> lignes;
}

