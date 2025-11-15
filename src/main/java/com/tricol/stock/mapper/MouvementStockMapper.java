package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.MouvementStockDTO;
import com.tricol.stock.entity.MouvementStock;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MouvementStockMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    @Mapping(source = "lot.id", target = "lotId")
    @Mapping(source = "lot.numeroLot", target = "lotNumero")
    MouvementStockDTO toDTO(MouvementStock entity);
    
    List<MouvementStockDTO> toDTOList(List<MouvementStock> entities);
}
