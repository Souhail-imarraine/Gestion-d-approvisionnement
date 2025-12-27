package com.tricol.stock.mapper;

import com.tricol.stock.dto.response.LotStockDTO;
import com.tricol.stock.entity.LotStock;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LotStockMapper {
    
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    @Mapping(source = "commande.id", target = "commandeId")
    @Mapping(source = "commande.numero", target = "commandeNumero")
    LotStockDTO toDTO(LotStock entity);
    
    List<LotStockDTO> toDTOList(List<LotStock> entities);
}
