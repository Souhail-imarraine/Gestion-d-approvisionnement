package com.tricol.stock.mapper;

import com.tricol.stock.dto.ProduitDTO;
import com.tricol.stock.entity.Produit;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProduitMapper {
    
    ProduitDTO toDTO(Produit entity);
    
    Produit toEntity(ProduitDTO dto);
    
    List<ProduitDTO> toDTOList(List<Produit> entities);
}
