package com.tricol.stock.service.impl;

import com.tricol.stock.dto.request.CommandeCreateRequest;
import com.tricol.stock.dto.request.CommandeUpdateRequest;
import com.tricol.stock.dto.request.LigneCommandeCreateRequest;
import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.entity.Commande;
import com.tricol.stock.entity.Fournisseur;
import com.tricol.stock.entity.LigneCommande;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.CommandeMapper;
import com.tricol.stock.repository.CommandeRepository;
import com.tricol.stock.repository.FournisseurRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {
    private final CommandeRepository repositoryCommande;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;

    @Override
    @Transactional
    public CommandeResponseDTO create(CommandeCreateRequest dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
            .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + dto.getFournisseurId()));

        Commande commande = new Commande();
        commande.setNumero(genererNumeroCommande());
        commande.setDateCommande(LocalDate.now());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setFournisseur(fournisseur);
        commande.setLignes(new ArrayList<>());

        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneCommandeCreateRequest ligneDTO : dto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

            LigneCommande ligneCommande = LigneCommande
                    .builder()
                    .produit(produit)
                    .quantite(ligneDTO.getQuantite())
                    .prixUnitaire(ligneDTO.getPrixUnitaire())
                    .sousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())))
                    .commande(commande)
                    .build();
            commande.getLignes().add(ligneCommande);
            montantTotal = montantTotal.add(ligneCommande.getSousTotal());
        }
        commande.setMontantTotal(montantTotal);

        Commande saved = repositoryCommande.save(commande);
        return commandeMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CommandeResponseDTO update(Long id, CommandeUpdateRequest commandeDTO) {
        Commande existingCommande = repositoryCommande.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (commandeDTO.getFournisseurId() != null){
            Fournisseur fournisseur = fournisseurRepository.findById(commandeDTO.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + commandeDTO.getFournisseurId()));
            existingCommande.setFournisseur(fournisseur);
        }

        if (commandeDTO.getDateLivraisonPrevue() != null){
            existingCommande.setDateLivraisonPrevue(commandeDTO.getDateLivraisonPrevue());
        }


        if (commandeDTO.getLignes() != null && !commandeDTO.getLignes().isEmpty()){
            existingCommande.getLignes().clear();

            BigDecimal montantTotal = BigDecimal.ZERO;

            for (LigneCommandeCreateRequest ligneDTO : commandeDTO.getLignes()) {
                Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

                LigneCommande ligne = LigneCommande
                        .builder()
                        .produit(produit)
                        .quantite(ligneDTO.getQuantite())
                        .prixUnitaire(ligneDTO.getPrixUnitaire())
                        .sousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())))
                        .commande(existingCommande)
                        .build();

                existingCommande.getLignes().add(ligne);
                montantTotal = montantTotal.add(ligne.getSousTotal());
            }

            existingCommande.setMontantTotal(montantTotal);
        }

        Commande updated = repositoryCommande.save(existingCommande);
        return commandeMapper.toDTO(updated);
    }

    @Override
    public CommandeResponseDTO findById(Long id) {
        Commande commande = repositoryCommande.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return commandeMapper.toDTO(commande);
    }

    @Override
    public List<CommandeResponseDTO> findAll() {
        return commandeMapper.toDTOList(repositoryCommande.findAll());
    }

    @Override
    public void delete(Long id) {
        if (!repositoryCommande.existsById(id)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id);
        }
        repositoryCommande.deleteById(id);
    }

    @Override
    public List<CommandeResponseDTO> findByStatut(StatutCommande statut) {
        return commandeMapper.toDTOList(repositoryCommande.findByStatut(statut));
    }

    @Override
    public List<CommandeResponseDTO> findByFournisseur(Long fournisseurId) {
        if (!fournisseurRepository.existsById(fournisseurId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + fournisseurId);
        }
        return commandeMapper.toDTOList(repositoryCommande.findByFournisseurId(fournisseurId));
    }

    @Override
    @Transactional
    public CommandeResponseDTO changerStatut(Long id, String nouveauStatut) {
        Commande commande = repositoryCommande.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        try {
            StatutCommande newStatus = StatutCommande.valueOf(nouveauStatut.toUpperCase());

            if (commande.getStatut() == StatutCommande.LIVREE) {
                throw new IllegalStateException("Une commande livrée ne peut plus changer de statut");
            }

            if (commande.getStatut() == StatutCommande.EN_ATTENTE && newStatus == StatutCommande.LIVREE) {
                throw new IllegalStateException("Une commande doit être VALIDEE avant d'être LIVREE");
            }

            commande.setStatut(newStatus);
            Commande updated = repositoryCommande.save(commande);
            return commandeMapper.toDTO(updated);

        }catch (IllegalArgumentException  e){
            throw new IllegalStateException("Statut invalide : " + nouveauStatut);

        }

    }


//    @Override
//    @Transactional
//    public CommandeResponseDTO changerStatut(Long id, ChangeStatusCommand statut) {
//        Commande commande = repositoryCommande.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
//
//        if (commande.getStatut() == StatutCommande.LIVREE) {
//            throw new IllegalStateException("Une commande livrée ne peut plus changer de statut");
//        }
//
//        if (commande.getStatut() == StatutCommande.EN_ATTENTE && statut.getStatut() != StatutCommande.LIVREE) {
//            throw new IllegalStateException("Une commande doit être VALIDEE avant d'être LIVREE");
//        }
//
//        commande.setStatut(statut.getStatut());
//        Commande updated = repositoryCommande.save(commande);
//        return commandeMapper.toDTO(updated);
//    }



    private String genererNumeroCommande() {
        return "CMD-" + System.currentTimeMillis();
    }
}
