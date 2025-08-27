package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Models.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProdutoMapper {

    @Mapping(target = "fornecedor.id", source = "fornecedorId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerUser", ignore = true)
    Produto toEntity(ProdutoRequest request);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    ProdutoRequest toRequest(Produto produto);
}
