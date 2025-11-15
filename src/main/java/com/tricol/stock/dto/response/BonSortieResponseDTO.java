package com.tricol.stock.dto.response;

import com.tricol.stock.enums.StatutBonSortie;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BonSortieResponseDTO {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;
    private StatutBonSortie statut;
    private String atelier;
    private String commentaire;
    private List<LigneBonSortieResponseDTO> lignes;
}
