package com.tricol.stock.service;

import com.tricol.stock.dto.response.CommandeResponseDTO;

public interface ReceptionService {
    CommandeResponseDTO receptionnerCommande(Long commandeId);
}
