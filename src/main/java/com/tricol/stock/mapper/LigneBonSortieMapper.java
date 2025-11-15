package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.LigneBonSortieResponseDTO;
import com.tricol.stock.entity.LigneBonSortie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LigneBonSortieMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    @Mapping(source = "produit.reference", target = "produitReference")
    LigneBonSortieResponseDTO toDTO(LigneBonSortie entity);
    
    List<LigneBonSortieResponseDTO> toDTOList(List<LigneBonSortie> entities);
}
