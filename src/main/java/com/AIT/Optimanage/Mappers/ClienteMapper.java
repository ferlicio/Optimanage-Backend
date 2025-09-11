package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Controllers.dto.ClienteResponse;
import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {

    @Mapping(target = "atividade", source = "atividadeId", qualifiedByName = "idToAtividade")
    @Mapping(target = "ownerUser", ignore = true)
    @Mapping(target = "ativo", source = "ativo", defaultValue = "true")
    Cliente toEntity(ClienteRequest request);

    @Mapping(target = "atividadeId", source = "atividade.id")
    ClienteRequest toRequest(Cliente cliente);

    @Mapping(target = "ownerUserId", source = "ownerUser.id")
    @Mapping(target = "atividadeId", source = "atividade.id")
    ClienteResponse toResponse(Cliente cliente);

    @Named("idToAtividade")
    default Atividade mapAtividade(Integer atividadeId) {
        if (atividadeId == null) {
            return null;
        }
        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);
        return atividade;
    }
}
