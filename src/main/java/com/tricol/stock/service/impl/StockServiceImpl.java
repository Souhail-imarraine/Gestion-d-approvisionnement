package com.tricol.stock.service.impl;

import com.tricol.stock.dto.MouvementStockDTO;
import com.tricol.stock.dto.StockDTO;
import com.tricol.stock.dto.StockDetailsDTO;
import com.tricol.stock.entity.LotStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.MouvementStockMapper;
import com.tricol.stock.repository.LotStockRepository;
import com.tricol.stock.repository.MouvementStockRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    
    private final ProduitRepository produitRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final MouvementStockMapper mouvementStockMapper;
    
    @Override
    public List<StockDTO> getStockOverview() {
        return produitRepository.findAll().stream()
            .map(this::mapToStockDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public StockDetailsDTO getStockDetails(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + produitId));
        
        List<LotStock> lots = lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(
            produitId, 0);
        
        StockDetailsDTO details = new StockDetailsDTO();
        details.setProduitId(produit.getId());
        details.setReference(produit.getReference());
        details.setNom(produit.getNom());
        details.setStockActuel(produit.getStockActuel());
        details.setPointCommande(produit.getPointCommande());
        details.setEnAlerte(produit.getStockActuel() < produit.getPointCommande());
        
        List<StockDetailsDTO.LotInfo> lotInfos = lots.stream()
            .map(lot -> {
                StockDetailsDTO.LotInfo info = new StockDetailsDTO.LotInfo();
                info.setNumeroLot(lot.getNumeroLot());
                info.setDateEntree(lot.getDateEntree());
                info.setQuantiteRestante(lot.getQuantiteRestante());
                info.setPrixUnitaire(lot.getPrixUnitaire());
                info.setValorisation(lot.getPrixUnitaire().multiply(BigDecimal.valueOf(lot.getQuantiteRestante())));
                return info;
            })
            .collect(Collectors.toList());
        
        details.setLots(lotInfos);
        details.setValorisationTotale(
            lotInfos.stream()
                .map(StockDetailsDTO.LotInfo::getValorisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        
        return details;
    }
    
    @Override
    public List<StockDTO> getStockAlertes() {
        return produitRepository.findProduitsEnAlerte().stream()
            .map(this::mapToStockDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<MouvementStockDTO> getMouvements(Long produitId) {
        return mouvementStockMapper.toDTOList(
            mouvementStockRepository.findByProduitIdOrderByDateMouvementDesc(produitId)
        );
    }
    
    @Override
    public List<MouvementStockDTO> getAllMouvements() {
        return mouvementStockMapper.toDTOList(mouvementStockRepository.findAll());
    }
    
    private StockDTO mapToStockDTO(Produit produit) {
        return new StockDTO(
            produit.getId(),
            produit.getNom(),
            produit.getStockActuel(),
            produit.getPointCommande(),
            produit.getUniteMesure(),
            produit.getStockActuel() < produit.getPointCommande()
        );
    }
}