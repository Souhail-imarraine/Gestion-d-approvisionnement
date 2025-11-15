package com.tricol.stock.mapper;

import com.tricol.stock.dto.request.FournisseurCreateRequest;
import com.tricol.stock.dto.request.FournisseurUpdateRequest;
import com.tricol.stock.dto.response.FournisseurResponseDTO;
import com.tricol.stock.entity.Fournisseur;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FournisseurMapper {

    FournisseurResponseDTO toResponseDTO(Fournisseur entity);

    @Mapping(target = "id", ignore = true)
    Fournisseur toEntity(FournisseurCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(FournisseurUpdateRequest dto, @MappingTarget Fournisseur entity);

    List<FournisseurResponseDTO> toResponseDTOList(List<Fournisseur> entities);
}
