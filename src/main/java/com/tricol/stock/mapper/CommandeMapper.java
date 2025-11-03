package com.tricol.stock.mapper;

import com.tricol.stock.dto.CommandeDTO;
import com.tricol.stock.entity.Commande;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LigneCommandeMapper.class})
public interface CommandeMapper {
    
    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.raisonSociale", target = "fournisseurNom")
    CommandeDTO toDTO(Commande entity);
    
    @Mapping(source = "fournisseurId", target = "fournisseur.id")
    @Mapping(target = "fournisseur.raisonSociale", ignore = true)
    Commande toEntity(CommandeDTO dto);
    
    List<CommandeDTO> toDTOList(List<Commande> entities);
}
