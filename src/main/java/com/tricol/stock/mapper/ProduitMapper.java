package com.tricol.stock.mapper;

import com.tricol.stock.dto.request.ProduitCreateRequest;
import com.tricol.stock.dto.request.ProduitUpdateRequest;
import com.tricol.stock.dto.response.ProduitResponseDTO;
import com.tricol.stock.entity.Produit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProduitMapper {
    
    ProduitResponseDTO toResponseDTO(Produit entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockActuel", ignore = true)
    Produit toEntity(ProduitCreateRequest dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockActuel", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProduitUpdateRequest dto, @MappingTarget Produit entity);
    
    List<ProduitResponseDTO> toResponseDTOList(List<Produit> entities);
}
