package com.tricol.stock.service;

import com.tricol.stock.dto.BonSortieDTO;

import java.util.List;

public interface BonSortieService {
    
    List<BonSortieDTO> findAll();
    
    BonSortieDTO findById(Long id);
    
    BonSortieDTO create(BonSortieDTO dto);
    
    BonSortieDTO update(Long id, BonSortieDTO dto);
    
    BonSortieDTO valider(Long id);
    
    void annuler(Long id);
    
    List<BonSortieDTO> findByAtelier(String atelier);
}