package com.tricol.stock.service.impl;
import com.tricol.stock.dto.FournisseurDTO;

import java.util.List;

public interface FournisseurImpl {

    FournisseurDTO create(FournisseurDTO dto);

    FournisseurDTO update(Long id, FournisseurDTO dto);

    FournisseurDTO findById(Long id);

    List<FournisseurDTO> findAll();

    void delete(Long id);
}
