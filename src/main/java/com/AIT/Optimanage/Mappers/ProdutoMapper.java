package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProdutoMapper {

    @Mapping(target = "fornecedor", source = "fornecedorId", qualifiedByName = "idToFornecedor")
    @Mapping(target = "ownerUser", ignore = true)
    Produto toEntity(ProdutoRequest request);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    ProdutoRequest toRequest(Produto produto);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    @Mapping(target = "ownerUserId", source = "ownerUser.id")
    ProdutoResponse toResponse(Produto produto);

    @Named("idToFornecedor")
    default Fornecedor mapFornecedor(Integer fornecedorId) {
        if (fornecedorId == null) {
            return null;
        }
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(fornecedorId);
        return fornecedor;
    }
}
