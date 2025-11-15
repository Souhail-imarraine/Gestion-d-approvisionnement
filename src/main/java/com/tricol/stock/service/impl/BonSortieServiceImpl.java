package com.tricol.stock.service.impl;

import com.tricol.stock.dto.request.BonSortieCreateRequest;
import com.tricol.stock.dto.request.BonSortieUpdateRequest;
import com.tricol.stock.dto.request.LigneBonSortieCreateRequest;
import com.tricol.stock.dto.response.BonSortieResponseDTO;
import com.tricol.stock.entity.BonSortie;
import com.tricol.stock.entity.LigneBonSortie;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.StatutBonSortie;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.BonSortieMapper;
import com.tricol.stock.repository.BonSortieRepository;
import com.tricol.stock.repository.MouvementStockRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.BonSortieService;
import com.tricol.stock.service.FifoStockStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonSortieServiceImpl implements BonSortieService {
    
    private final BonSortieRepository bonSortieRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitRepository produitRepository;
    private final FifoStockStrategy fifoStrategy;
    private final BonSortieMapper bonSortieMapper;
    
    @Override
    public List<BonSortieResponseDTO> findAll() {
        return bonSortieMapper.toDTOList(bonSortieRepository.findAll());
    }
    
    @Override
    public BonSortieResponseDTO findById(Long id) {
        BonSortie bonSortie = bonSortieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie non trouvé: " + id));
        return bonSortieMapper.toDTO(bonSortie);
    }
    
    @Override
    @Transactional
    public BonSortieResponseDTO create(BonSortieCreateRequest dto) {
        BonSortie bonSortie = new BonSortie();
        bonSortie.setNumero(genererNumero());
        bonSortie.setDateCreation(LocalDateTime.now());
        bonSortie.setStatut(StatutBonSortie.BROUILLON);
        bonSortie.setAtelier(dto.getAtelier());
        bonSortie.setCommentaire(dto.getCommentaire());
        
        for (LigneBonSortieCreateRequest ligneDTO : dto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + ligneDTO.getProduitId()));

            LigneBonSortie ligne = new LigneBonSortie();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setBonSortie(bonSortie);
            
            bonSortie.getLignes().add(ligne);
        }
        
        BonSortie saved = bonSortieRepository.save(bonSortie);
        return bonSortieMapper.toDTO(saved);
    }
    
    @Override
    @Transactional
    public BonSortieResponseDTO update(Long id, BonSortieUpdateRequest dto) {
        BonSortie existing = bonSortieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie non trouvé: " + id));
        
        if (existing.getStatut() != StatutBonSortie.BROUILLON) {
            throw new IllegalStateException("Seuls les bons BROUILLON peuvent être modifiés");
        }

        if (dto.getAtelier() != null) {
            existing.setAtelier(dto.getAtelier());
        }
        if (dto.getCommentaire() != null) {
            existing.setCommentaire(dto.getCommentaire());
        }
        
        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            existing.getLignes().clear();
            
            for (LigneBonSortieCreateRequest ligneDTO : dto.getLignes()) {
                Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + ligneDTO.getProduitId()));
                
                LigneBonSortie ligne = new LigneBonSortie();
                ligne.setProduit(produit);
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setBonSortie(existing);
                
                existing.getLignes().add(ligne);
            }
        }
        
        BonSortie updated = bonSortieRepository.save(existing);
        return bonSortieMapper.toDTO(updated);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BonSortieResponseDTO valider(Long id) {
        BonSortie bon = bonSortieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie non trouvé: " + id));

        if (bon.getStatut() != StatutBonSortie.BROUILLON) {
            throw new IllegalStateException("Seuls les bons BROUILLON peuvent être validés");
        }

        for (LigneBonSortie ligne : bon.getLignes()) {
            List<MouvementStock> mouvements = fifoStrategy.consumeStock(ligne.getProduit(), ligne.getQuantite());

            mouvements.forEach(m -> m.setReference("BS-" + bon.getNumero()));
            mouvementStockRepository.saveAll(mouvements);

            ligne.getProduit().setStockActuel(
                ligne.getProduit().getStockActuel() - ligne.getQuantite()
            );
            produitRepository.save(ligne.getProduit());
        }

        bon.setStatut(StatutBonSortie.VALIDE);
        bon.setDateValidation(LocalDateTime.now());

        BonSortie validated = bonSortieRepository.save(bon);
        return bonSortieMapper.toDTO(validated);
    }
    
    @Override
    @Transactional
    public void annuler(Long id) {
        BonSortie bon = bonSortieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie non trouvé: " + id));
        
        if (bon.getStatut() == StatutBonSortie.VALIDE) {
            throw new IllegalStateException("Les bons validés ne peuvent pas être annulés");
        }
        
        bon.setStatut(StatutBonSortie.ANNULE);
        bonSortieRepository.save(bon);
    }
    
    @Override
    public List<BonSortieResponseDTO> findByAtelier(String atelier) {
        return bonSortieMapper.toDTOList(bonSortieRepository.findByAtelier(atelier));
    }
    
    private String genererNumero() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = bonSortieRepository.count() + 1;
        return String.format("BS-%s-%04d", date, count);
    }
}
