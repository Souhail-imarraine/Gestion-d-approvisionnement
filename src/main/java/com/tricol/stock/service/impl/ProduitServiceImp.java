package com.tricol.stock.service.impl;

import com.tricol.stock.dto.ProduitDTO;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.ProduitMapper;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
public class ProduitServiceImp implements ProduitService {
    private final ProduitRepository repository;
    private final ProduitMapper ProduitMapper;

    public List<ProduitDTO> findAll() {
        return ProduitMapper.toDTOList(repository.findAll());
    }

    public ProduitDTO findById(Long id) {
        Produit produit = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));
        return ProduitMapper.toDTO(produit);
    }

    public ProduitDTO create(ProduitDTO dto) {
        Produit produit = ProduitMapper.toEntity(dto);
        Produit saved = repository.save(produit);
        return ProduitMapper.toDTO(saved);
    }

    public ProduitDTO update(Long id, ProduitDTO dto) {
        Produit existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        existing.setReference(dto.getReference());
        existing.setNom(dto.getNom());
        existing.setDescription(dto.getDescription());
        existing.setPrixUnitaire(dto.getPrixUnitaire());
        existing.setCategorie(dto.getCategorie());
        existing.setStockActuel(dto.getStockActuel());
        existing.setPointCommande(dto.getPointCommande());
        existing.setUniteMesure(dto.getUniteMesure());

        Produit updated = repository.save(existing);
        return ProduitMapper.toDTO(updated);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id);
        }
        repository.deleteById(id);
    }

    public List<ProduitDTO> findProduitsEnAlerte() {
        return ProduitMapper.toDTOList(repository.findProduitsEnAlerte());
    }
}
