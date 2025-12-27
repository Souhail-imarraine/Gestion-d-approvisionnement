package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.BonSortieResponseDTO;
import com.tricol.stock.entity.BonSortie;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LigneBonSortieMapper.class})
public interface BonSortieMapper {
    
    BonSortieResponseDTO toDTO(BonSortie entity);
    
    List<BonSortieResponseDTO> toDTOList(List<BonSortie> entities);
}
