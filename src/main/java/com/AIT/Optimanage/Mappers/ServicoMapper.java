package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicoMapper {

    @Mapping(target = "fornecedor", source = "fornecedorId", qualifiedByName = "idToFornecedor")
    @Mapping(target = "ownerUser", ignore = true)
    Servico toEntity(ServicoRequest request);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    @Mapping(target = "ownerUserId", source = "ownerUser.id")
    ServicoResponse toResponse(Servico servico);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    ServicoRequest toRequest(Servico servico);

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

