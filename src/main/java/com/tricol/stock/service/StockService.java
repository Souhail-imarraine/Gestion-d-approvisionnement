package com.tricol.stock.service;

import com.tricol.stock.dto.response.*;

import java.util.List;

public interface StockService {
    List<EtatStockDTO> getEtatGlobalStock();
    DetailStockProduitDTO getDetailStockProduit(Long produitId);
    List<MouvementStockDTO> getHistoriqueMouvements();
    List<MouvementStockDTO> getMouvementsProduit(Long produitId);
    List<EtatStockDTO> getProduitsEnAlerte();
    ValorisationStockDTO getValorisationStock();
//    MouvementStockDTO searchingMouvement();
}
