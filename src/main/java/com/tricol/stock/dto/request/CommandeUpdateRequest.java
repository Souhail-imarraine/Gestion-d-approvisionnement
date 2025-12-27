package com.tricol.stock.dto.request;

import com.tricol.stock.enums.StatutCommande;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeUpdateRequest {

    @FutureOrPresent
    private LocalDate dateLivraisonPrevue;

    private StatutCommande statut;

    private Long fournisseurId;

    @Valid
    private List<LigneCommandeCreateRequest> lignes;
}
