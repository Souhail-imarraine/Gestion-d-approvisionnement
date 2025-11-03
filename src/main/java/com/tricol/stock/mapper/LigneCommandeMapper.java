package com.tricol.stock.mapper;

import com.tricol.stock.dto.LigneCommandeDTO;
import com.tricol.stock.entity.LigneCommande;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LigneCommandeMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    LigneCommandeDTO toDTO(LigneCommande entity);
    
    @Mapping(source = "produitId", target = "produit.id")
    @Mapping(target = "produit.nom", ignore = true)
    @Mapping(target = "commande", ignore = true)
    LigneCommande toEntity(LigneCommandeDTO dto);
    
    List<LigneCommandeDTO> toDTOList(List<LigneCommande> entities);
}
