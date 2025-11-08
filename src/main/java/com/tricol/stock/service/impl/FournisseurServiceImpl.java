package com.tricol.stock.service.impl;

import com.tricol.stock.dto.FournisseurDTO;
import com.tricol.stock.entity.Fournisseur;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.FournisseurMapper;
import com.tricol.stock.repository.FournisseurRepository;
import com.tricol.stock.service.FournisseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository repositoryFournisseur;
    private final FournisseurMapper FournisseurMapper;

    @Override
    public List<FournisseurDTO> findAll() {
        return FournisseurMapper.toDTOList(repositoryFournisseur.findAll());
    }

    @Override
    public FournisseurDTO findById(Long id) {
        Fournisseur fournisseur = repositoryFournisseur.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        return FournisseurMapper.toDTO(fournisseur);
    }

    @Override
    public FournisseurDTO create(FournisseurDTO dto) {
        if(dto.getEmail() != null && repositoryFournisseur.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("l'email" + dto.getEmail() + " existe déjà ");
        }
        if (dto.getIce() != null && repositoryFournisseur.existsByIce(dto.getIce())) {
            throw new IllegalArgumentException("Un fournisseur avec l'ICE " + dto.getIce() + " existe déjà");
        }
        Fournisseur fournisseur = FournisseurMapper.toEntity(dto);
        Fournisseur saved = repositoryFournisseur.save(fournisseur);
        return FournisseurMapper.toDTO(saved);
    }

    @Override
    public FournisseurDTO update(Long id, FournisseurDTO dto) {
        Fournisseur existing = repositoryFournisseur.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));

        if(dto.getEmail() != null && repositoryFournisseur.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("l'email" + dto.getEmail() + " existe déjà ");
        }
        if (dto.getIce() != null && repositoryFournisseur.existsByIce(dto.getIce())) {
            throw new IllegalArgumentException("Un fournisseur avec l'ICE " + dto.getIce() + " existe déjà");
        }

        existing.setRaisonSociale(dto.getRaisonSociale());
        existing.setAdresse(dto.getAdresse());
        existing.setVille(dto.getVille());
        existing.setPersonneContact(dto.getPersonneContact());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setIce(dto.getIce());

        Fournisseur updated = repositoryFournisseur.save(existing);
        return FournisseurMapper.toDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repositoryFournisseur.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id);
        }
        repositoryFournisseur.deleteById(id);
    }

    @Override
    public List<FournisseurDTO> searchByName(String name){
        List<Fournisseur> fournisseurs = repositoryFournisseur.findByRaisonSocialeContainingIgnoreCase(name);
        return FournisseurMapper.toDTOList(fournisseurs);
    }
}
