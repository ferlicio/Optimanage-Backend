package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FornecedorMapper {

    @Mapping(target = "atividade", source = "atividadeId", qualifiedByName = "idToAtividade")
    @Mapping(target = "ownerUser", ignore = true)
    Fornecedor toEntity(FornecedorRequest request);

    @Mapping(target = "atividadeId", source = "atividade.id")
    FornecedorRequest toRequest(Fornecedor fornecedor);

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
