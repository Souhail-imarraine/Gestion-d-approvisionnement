package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.LigneCommandeResponseDTO;
import com.tricol.stock.entity.LigneCommande;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LigneCommandeMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    LigneCommandeResponseDTO toResponseDTO(LigneCommande entity);
    
    List<LigneCommandeResponseDTO> toResponseDTOList(List<LigneCommande> entities);
}
