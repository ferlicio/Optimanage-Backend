package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Models.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicoMapper {

    @Mapping(target = "fornecedor.id", source = "fornecedorId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerUser", ignore = true)
    Servico toEntity(ServicoRequest request);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    @Mapping(target = "ownerUserId", source = "ownerUser.id")
    ServicoResponse toResponse(Servico servico);

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    ServicoRequest toRequest(Servico servico);
}

