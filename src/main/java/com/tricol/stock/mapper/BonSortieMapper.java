package com.tricol.stock.mapper;

import com.tricol.stock.dto.BonSortieDTO;
import com.tricol.stock.entity.BonSortie;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LigneBonSortieMapper.class})
public interface BonSortieMapper {
    
    BonSortieDTO toDTO(BonSortie entity);
    
    BonSortie toEntity(BonSortieDTO dto);
    
    List<BonSortieDTO> toDTOList(List<BonSortie> entities);
}