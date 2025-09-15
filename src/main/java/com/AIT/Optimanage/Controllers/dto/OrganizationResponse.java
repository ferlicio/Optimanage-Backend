package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Integer id;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private Integer ownerUserId;
}
