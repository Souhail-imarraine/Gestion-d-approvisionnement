package com.tricol.stock.mapper;

import com.tricol.stock.dto.FournisseurDTO;
import com.tricol.stock.entity.Fournisseur;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FournisseurMapper {

    FournisseurDTO toDTO(Fournisseur entity);

    Fournisseur toEntity(FournisseurDTO dto);

    List<FournisseurDTO> toDTOList(List<Fournisseur> entities);
}
