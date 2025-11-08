package com.tricol.stock.service;

import com.tricol.stock.dto.FournisseurDTO;
import com.tricol.stock.entity.Fournisseur;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.FournisseurMapper;
import com.tricol.stock.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository repository;
    private final FournisseurMapper mapper;

    public List<FournisseurDTO> findAll() {
        return mapper.toDTOList(repository.findAll());
    }

    public FournisseurDTO findById(Long id) {
        Fournisseur fournisseur = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        return mapper.toDTO(fournisseur);
    }

    public FournisseurDTO create(FournisseurDTO dto) {
        Fournisseur fournisseur = mapper.toEntity(dto);
        Fournisseur saved = repository.save(fournisseur);
        return mapper.toDTO(saved);
    }

    public FournisseurDTO update(Long id, FournisseurDTO dto) {
        Fournisseur existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));

        existing.setRaisonSociale(dto.getRaisonSociale());
        existing.setAdresse(dto.getAdresse());
        existing.setVille(dto.getVille());
        existing.setPersonneContact(dto.getPersonneContact());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setIce(dto.getIce());

        Fournisseur updated = repository.save(existing);
        return mapper.toDTO(updated);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id);
        }
        repository.deleteById(id);
    }
}
