package com.tricol.stock.service;

import com.tricol.stock.dto.ProduitDTO;


import java.util.List;

public interface  ProduitService {
    ProduitDTO create(ProduitDTO dto);
    ProduitDTO update(Long id, ProduitDTO dto);
    ProduitDTO findById(Long id);
    List<ProduitDTO> findAll();
    void delete(Long id);
    List<ProduitDTO> findProduitsEnAlerte();
}
