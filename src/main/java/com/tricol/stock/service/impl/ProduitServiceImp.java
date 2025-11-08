package com.tricol.stock.service.impl;

import com.tricol.stock.dto.ProduitDTO;
import com.tricol.stock.dto.StockDTO;
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

    public List<ProduitDTO> findAll() {
        return produitMapper.toDTOList(produitRepository.findAll());
    }

    @Override
    public ProduitDTO findById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));
        return produitMapper.toDTO(produit);
    }

    @Override
    public ProduitDTO create(ProduitDTO dto) {
        if(dto.getReference() != null && produitRepository.existsByreference(dto.getReference())){
            throw new DuplicateReferenceException("La référence" +dto.getReference()+" existe déjà");
        }
        Produit produit = produitMapper.toEntity(dto);
        Produit saved = produitRepository.save(produit);
        return produitMapper.toDTO(saved);
    }

    @Override
    public ProduitDTO update(Long id, ProduitDTO dto) {
        Produit existing = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        existing.setReference(dto.getReference());
        existing.setNom(dto.getNom());
        existing.setDescription(dto.getDescription());
        existing.setPrixUnitaire(dto.getPrixUnitaire());
        existing.setCategorie(dto.getCategorie());
        existing.setStockActuel(dto.getStockActuel());
        existing.setPointCommande(dto.getPointCommande());
        existing.setUniteMesure(dto.getUniteMesure());

        Produit updated = produitRepository.save(existing);
        return produitMapper.toDTO(updated);
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

    public List<ProduitDTO> findProduitsEnAlerte() {
        return produitMapper.toDTOList(produitRepository.findProduitsEnAlerte());
    }
}
