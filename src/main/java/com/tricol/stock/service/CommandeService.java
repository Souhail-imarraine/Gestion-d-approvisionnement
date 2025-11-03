package com.tricol.stock.service;

import com.tricol.stock.dto.CommandeDTO;
import com.tricol.stock.enums.StatutCommande;

import java.util.List;

public interface CommandeService {
    CommandeDTO create(CommandeDTO dto);
    CommandeDTO update(Long id, CommandeDTO dto);
    CommandeDTO findById(Long id);
    List<CommandeDTO> findAll();
    void delete(Long id);
    List<CommandeDTO> findByStatut(StatutCommande statut);
    List<CommandeDTO> findByFournisseur(Long fournisseurId);
    CommandeDTO changerStatut(Long id, StatutCommande nouveauStatut);
}
