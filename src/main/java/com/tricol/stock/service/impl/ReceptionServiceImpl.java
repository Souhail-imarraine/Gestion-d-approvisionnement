package com.tricol.stock.service.impl;

import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.entity.*;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.CommandeMapper;
import com.tricol.stock.repository.*;
import com.tricol.stock.service.ReceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReceptionServiceImpl implements ReceptionService {
    
    private final CommandeRepository commandeRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;

    @Override
    @Transactional
    public CommandeResponseDTO receptionnerCommande(Long commandeId) {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + commandeId));

        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new IllegalStateException("Seules les commandes VALIDÉES peuvent être réceptionnées. Statut actuel: " + commande.getStatut());
        }

        for (LigneCommande ligne : commande.getLignes()) {

            String numeroLot = genererNumeroLot();

            LotStock lot = new LotStock();
            lot.setNumeroLot(numeroLot);
            lot.setDateEntree(LocalDate.now());
            lot.setQuantiteInitiale(ligne.getQuantite());
            lot.setQuantiteRestante(ligne.getQuantite());
            lot.setPrixUnitaire(ligne.getPrixUnitaire());
            lot.setProduit(ligne.getProduit());
            lot.setCommande(commande);
            lotStockRepository.save(lot);
            
            MouvementStock mouvement = new MouvementStock();
            mouvement.setDateMouvement(LocalDate.now());
            mouvement.setTypeMouvement(TypeMouvement.ENTREE);
            mouvement.setQuantite(ligne.getQuantite());
            mouvement.setPrixUnitaire(ligne.getPrixUnitaire());
            mouvement.setReference(commande.getNumero());
            mouvement.setProduit(ligne.getProduit());
            mouvement.setLot(lot);
            mouvementStockRepository.save(mouvement);

            Produit produit = ligne.getProduit();
            produit.setStockActuel(produit.getStockActuel() + ligne.getQuantite());
            produitRepository.save(produit);
        }

        commande.setStatut(StatutCommande.LIVREE);
        Commande updated = commandeRepository.save(commande);
        return commandeMapper.toDTO(updated);
    }

    private String genererNumeroLot() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = lotStockRepository.count() + 1;
        return String.format("LOT-%s-%04d", date, count);
    }
}
