package com.tricol.stock.mapper;

import com.tricol.stock.dto.LigneBonSortieDTO;
import com.tricol.stock.entity.LigneBonSortie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LigneBonSortieMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    @Mapping(source = "produit.reference", target = "produitReference")
    LigneBonSortieDTO toDTO(LigneBonSortie entity);
    
    @Mapping(source = "produitId", target = "produit.id")
    LigneBonSortie toEntity(LigneBonSortieDTO dto);
    
    List<LigneBonSortieDTO> toDTOList(List<LigneBonSortie> entities);
}