package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Models.Plano;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanoMapper {
    Plano toEntity(PlanoRequest request);
    PlanoResponse toResponse(Plano plano);
}
