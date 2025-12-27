package com.tricol.stock.service;

import com.tricol.stock.dto.request.BonSortieCreateRequest;
import com.tricol.stock.dto.request.BonSortieUpdateRequest;
import com.tricol.stock.dto.response.BonSortieResponseDTO;

import java.util.List;

public interface BonSortieService {
    
    List<BonSortieResponseDTO> findAll();
    
    BonSortieResponseDTO findById(Long id);
    
    BonSortieResponseDTO create(BonSortieCreateRequest dto);
    
    BonSortieResponseDTO update(Long id, BonSortieUpdateRequest dto);
    
    BonSortieResponseDTO valider(Long id);
    
    void annuler(Long id);
    
    List<BonSortieResponseDTO> findByAtelier(String atelier);
}
