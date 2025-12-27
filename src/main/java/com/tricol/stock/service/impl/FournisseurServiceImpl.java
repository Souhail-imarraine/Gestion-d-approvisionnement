package com.tricol.stock.service.impl;

import com.tricol.stock.dto.request.FournisseurCreateRequest;
import com.tricol.stock.dto.request.FournisseurUpdateRequest;
import com.tricol.stock.dto.response.FournisseurResponseDTO;
import com.tricol.stock.entity.Fournisseur;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.FournisseurMapper;
import com.tricol.stock.repository.CommandeRepository;
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
    private final CommandeRepository commandeRepository;

    @Override
    public List<FournisseurResponseDTO> findAll() {
        return FournisseurMapper.toResponseDTOList(repositoryFournisseur.findAll());
    }

    @Override
    public FournisseurResponseDTO findById(Long id) {
        Fournisseur fournisseur = repositoryFournisseur.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        return FournisseurMapper.toResponseDTO(fournisseur);
    }

    @Override
    public FournisseurResponseDTO create(FournisseurCreateRequest dto) {
        if(dto.getEmail() != null && repositoryFournisseur.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("l'email " + dto.getEmail() + " existe déjà");
        }
        if (dto.getIce() != null && repositoryFournisseur.existsByIce(dto.getIce())) {
            throw new IllegalArgumentException("Un fournisseur avec l'ICE " + dto.getIce() + " existe déjà");
        }
        Fournisseur fournisseur = FournisseurMapper.toEntity(dto);
        Fournisseur saved = repositoryFournisseur.save(fournisseur);
        return FournisseurMapper.toResponseDTO(saved);
    }

    @Override
    public FournisseurResponseDTO update(Long id, FournisseurUpdateRequest dto) {
        Fournisseur existing = repositoryFournisseur.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));

        if(dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail()) && repositoryFournisseur.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("l'email " + dto.getEmail() + " existe déjà");
        }
        if (dto.getIce() != null && !dto.getIce().equals(existing.getIce()) && repositoryFournisseur.existsByIce(dto.getIce())) {
            throw new IllegalArgumentException("Un fournisseur avec l'ICE " + dto.getIce() + " existe déjà");
        }

        FournisseurMapper.updateEntityFromDto(dto, existing);
        Fournisseur updated = repositoryFournisseur.save(existing);
        return FournisseurMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repositoryFournisseur.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id);
        }

        if (!commandeRepository.findByFournisseurId(id).isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer ce fournisseur car il a des commandes associées");
        }
        repositoryFournisseur.deleteById(id);
    }

    @Override
    public List<FournisseurResponseDTO> searchByName(String name){
        List<Fournisseur> fournisseurs = repositoryFournisseur.findByRaisonSocialeContainingIgnoreCase(name);
        return FournisseurMapper.toResponseDTOList(fournisseurs);
    }
}
