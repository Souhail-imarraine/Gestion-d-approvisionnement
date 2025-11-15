package com.tricol.stock.service;

import com.tricol.stock.dto.request.CommandeCreateRequest;
import com.tricol.stock.dto.request.CommandeUpdateRequest;
import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.enums.StatutCommande;

import java.util.List;

public interface CommandeService {
    CommandeResponseDTO create(CommandeCreateRequest dto);
    CommandeResponseDTO update(Long id, CommandeUpdateRequest dto);
    CommandeResponseDTO findById(Long id);
    List<CommandeResponseDTO> findAll();
    void delete(Long id);
    List<CommandeResponseDTO> findByStatut(StatutCommande statut);
    List<CommandeResponseDTO> findByFournisseur(Long fournisseurId);
    CommandeResponseDTO changerStatut(Long id, String nouveauStatut);
//    CommandeResponseDTO  changerStatut(Long id, ChangeStatusCommand statut);
}
