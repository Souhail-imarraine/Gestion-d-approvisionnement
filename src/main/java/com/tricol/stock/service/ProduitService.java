package com.tricol.stock.service;

import com.tricol.stock.dto.response.StockDTO;
import com.tricol.stock.dto.request.ProduitCreateRequest;
import com.tricol.stock.dto.request.ProduitUpdateRequest;
import com.tricol.stock.dto.response.ProduitResponseDTO;

import java.util.List;

public interface ProduitService {
    ProduitResponseDTO create(ProduitCreateRequest dto);
    ProduitResponseDTO update(Long id, ProduitUpdateRequest dto);
    ProduitResponseDTO findById(Long id);
    List<ProduitResponseDTO> findAll();
    void delete(Long id);
    List<ProduitResponseDTO> findProduitsEnAlerte();
    StockDTO getStock(Long id);
}
