<<<<<<< HEAD
package com.tricol.stock.service.impl;

import com.tricol.stock.dto.CommandeDTO;
import com.tricol.stock.dto.LigneCommandeDTO;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository repository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;

    @Override
    @Transactional
    public CommandeDTO create(CommandeDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId()).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + dto.getFournisseurId()));

        Commande commande = new Commande();
        commande.setNumero(dto.getNumero());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(dto.getStatut());
        commande.setFournisseur(fournisseur);

        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneCommandeDTO ligneDTO : dto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId()).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
            ligne.setSousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));

            ligne.setCommande(commande);

            commande.getLignes().add(ligne);
            montantTotal = montantTotal.add(ligne.getSousTotal());
        }
        commande.setMontantTotal(montantTotal);

        Commande saved = repository.save(commande);
        return commandeMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CommandeDTO update(Long id, CommandeDTO commandeDTO) {
        Commande existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        Fournisseur fournisseur = fournisseurRepository.findById(commandeDTO.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + commandeDTO.getFournisseurId()));

        existing.setNumero(commandeDTO.getNumero());
        existing.setDateCommande(commandeDTO.getDateCommande());
        existing.setDateLivraisonPrevue(commandeDTO.getDateLivraisonPrevue());
        existing.setStatut(commandeDTO.getStatut());
        existing.setFournisseur(fournisseur);

        existing.getLignes().clear();


        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneCommandeDTO ligneDTO : commandeDTO.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
            ligne.setSousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));
            ligne.setCommande(existing);

            existing.getLignes().add(ligne);
            montantTotal = montantTotal.add(ligne.getSousTotal());
        }

        existing.setMontantTotal(montantTotal);

        Commande updated = repository.save(existing);
        return commandeMapper.toDTO(updated);
    }

    @Override
    public CommandeDTO findById(Long id) {
        Commande commande = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return commandeMapper.toDTO(commande);
    }

    @Override
    public List<CommandeDTO> findAll() {
        return commandeMapper.toDTOList(repository.findAll());
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<CommandeDTO> findByStatut(StatutCommande statut) {
        return commandeMapper.toDTOList(repository.findByStatut(statut));
    }

    @Override
    public List<CommandeDTO> findByFournisseur(Long fournisseurId) {
        return commandeMapper.toDTOList(repository.findByFournisseurId(fournisseurId));
    }

    @Override
    @Transactional
    public CommandeDTO changerStatut(Long id, StatutCommande nouveauStatut) {
        Commande commande = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        commande.setStatut(nouveauStatut);
        Commande updated = repository.save(commande);
        return commandeMapper.toDTO(updated);
    }
}
=======
package com.tricol.stock.service.impl;

import com.tricol.stock.dto.CommandeDTO;
import com.tricol.stock.dto.LigneCommandeDTO;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository repository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;

    @Override
    @Transactional
    public CommandeDTO create(CommandeDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId()).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + dto.getFournisseurId()));

        Commande commande = new Commande();
        commande.setNumero(dto.getNumero());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(dto.getStatut());
        commande.setFournisseur(fournisseur);

        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneCommandeDTO ligneDTO : dto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId()).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
            ligne.setSousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));

            ligne.setCommande(commande);

            commande.getLignes().add(ligne);
            montantTotal = montantTotal.add(ligne.getSousTotal());
        }
        commande.setMontantTotal(montantTotal);

        Commande saved = repository.save(commande);
        return commandeMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CommandeDTO update(Long id, CommandeDTO commandeDTO) {
        Commande existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        Fournisseur fournisseur = fournisseurRepository.findById(commandeDTO.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + commandeDTO.getFournisseurId()));

        existing.setNumero(commandeDTO.getNumero());
        existing.setDateCommande(commandeDTO.getDateCommande());
        existing.setDateLivraisonPrevue(commandeDTO.getDateLivraisonPrevue());
        existing.setStatut(commandeDTO.getStatut());
        existing.setFournisseur(fournisseur);

        existing.getLignes().clear();


        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneCommandeDTO ligneDTO : commandeDTO.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + ligneDTO.getProduitId()));

            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
            ligne.setSousTotal(ligneDTO.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));
            ligne.setCommande(existing);

            existing.getLignes().add(ligne);
            montantTotal = montantTotal.add(ligne.getSousTotal());
        }

        existing.setMontantTotal(montantTotal);

        Commande updated = repository.save(existing);
        return commandeMapper.toDTO(updated);
    }

    @Override
    public CommandeDTO findById(Long id) {
        Commande commande = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return commandeMapper.toDTO(commande);
    }

    @Override
    public List<CommandeDTO> findAll() {
        return commandeMapper.toDTOList(repository.findAll());
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<CommandeDTO> findByStatut(StatutCommande statut) {
        return commandeMapper.toDTOList(repository.findByStatut(statut));
    }

    @Override
    public List<CommandeDTO> findByFournisseur(Long fournisseurId) {
        return commandeMapper.toDTOList(repository.findByFournisseurId(fournisseurId));
    }

    @Override
    @Transactional
    public CommandeDTO changerStatut(Long id, StatutCommande nouveauStatut) {
        Commande commande = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        commande.setStatut(nouveauStatut);
        Commande updated = repository.save(commande);
        return commandeMapper.toDTO(updated);
    }
}
>>>>>>> 59833a1 (add validation fourniseur)
