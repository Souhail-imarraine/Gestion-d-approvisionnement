package com.tricol.stock.service;

import com.tricol.stock.dto.StockDetailsDTO;
import com.tricol.stock.dto.StockDTO;
import com.tricol.stock.dto.MouvementStockDTO;

import java.util.List;

public interface StockService {
    
    List<StockDTO> getStockOverview();
    
    StockDetailsDTO getStockDetails(Long produitId);
    
    List<StockDTO> getStockAlertes();
    
    List<MouvementStockDTO> getMouvements(Long produitId);
    
    List<MouvementStockDTO> getAllMouvements();
}