package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.entity.Commande;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LigneCommandeMapper.class})
public interface CommandeMapper {
    
    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.raisonSociale", target = "fournisseurNom")
    @Mapping(source = "lignes", target = "lignes")
    CommandeResponseDTO toDTO(Commande entity);
    
    List<CommandeResponseDTO> toDTOList(List<Commande> entities);
}
