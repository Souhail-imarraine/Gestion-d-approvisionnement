package com.tricol.stock.service;
import com.tricol.stock.dto.FournisseurDTO;

import java.util.List;

public interface FournisseurService {

    FournisseurDTO create(FournisseurDTO dto);

    FournisseurDTO update(Long id, FournisseurDTO dto);

    FournisseurDTO findById(Long id);

    List<FournisseurDTO> findAll();

    void delete(Long id);

    List<FournisseurDTO> searchByName(String name);
}
