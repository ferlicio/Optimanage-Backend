package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.AIT.Optimanage.Models.Organization.TrialType;

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
    private LocalDate trialInicio;
    private LocalDate trialFim;
    private TrialType trialTipo;
}
