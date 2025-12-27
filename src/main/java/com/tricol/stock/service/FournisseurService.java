package com.tricol.stock.service;

import com.tricol.stock.dto.request.FournisseurCreateRequest;
import com.tricol.stock.dto.request.FournisseurUpdateRequest;
import com.tricol.stock.dto.response.FournisseurResponseDTO;

import java.util.List;

public interface FournisseurService {

    FournisseurResponseDTO create(FournisseurCreateRequest dto);

    FournisseurResponseDTO update(Long id, FournisseurUpdateRequest dto);

    FournisseurResponseDTO findById(Long id);

    List<FournisseurResponseDTO> findAll();

    void delete(Long id);

    List<FournisseurResponseDTO> searchByName(String name);
}
