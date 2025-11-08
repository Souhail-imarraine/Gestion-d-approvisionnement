package com.tricol.stock.service;

import com.tricol.stock.dto.CommandeDTO;

public interface ReceptionService {
    CommandeDTO receptionnerCommande(Long commandeId);
}
