package com.tricol.stock.service.impl;

import com.tricol.stock.dto.request.ProduitCreateRequest;
import com.tricol.stock.dto.request.ProduitUpdateRequest;
import com.tricol.stock.dto.response.ProduitResponseDTO;
import com.tricol.stock.dto.response.StockDTO;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.exception.DuplicateReferenceException;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.ProduitMapper;
import com.tricol.stock.repository.LigneCommandeRepository;
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
    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;
    private final LigneCommandeRepository ligneCommandeRepository;

    public List<ProduitResponseDTO> findAll() {
        return produitMapper.toResponseDTOList(produitRepository.findAll());
    }

    @Override
    public ProduitResponseDTO findById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));
        return produitMapper.toResponseDTO(produit);
    }

    @Override
    public ProduitResponseDTO create(ProduitCreateRequest dto) {
        if(dto.getReference() != null && produitRepository.existsByreference(dto.getReference())){
            throw new DuplicateReferenceException("La référence " + dto.getReference() + " existe déjà");
        }
        Produit produit = produitMapper.toEntity(dto);
        produit.setStockActuel(0);
        Produit saved = produitRepository.save(produit);
        return produitMapper.toResponseDTO(saved);
    }

    @Override
    public ProduitResponseDTO update(Long id, ProduitUpdateRequest dto) {
        Produit existing = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        if(dto.getReference() != null && !dto.getReference().equals(existing.getReference()) && produitRepository.existsByreference(dto.getReference())){
            throw new DuplicateReferenceException("La référence " + dto.getReference() + " existe déjà");
        }

        produitMapper.updateEntityFromDto(dto, existing);
        Produit updated = produitRepository.save(existing);
        return produitMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id);
        }
        if (!ligneCommandeRepository.findByProduitId(id).isEmpty()){
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer un produit qui existe dans une commande");
        }
        produitRepository.deleteById(id);
    }

    @Override
    public StockDTO getStock(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        boolean enAlerte = produit.getStockActuel() <= produit.getPointCommande();

        return new StockDTO(
                produit.getId(),
                produit.getNom(),
                produit.getStockActuel(),
                produit.getPointCommande(),
                produit.getUniteMesure(),
                enAlerte
        );
    }

    public List<ProduitResponseDTO> findProduitsEnAlerte() {
        return produitMapper.toResponseDTOList(produitRepository.findProduitsEnAlerte());
    }
}
