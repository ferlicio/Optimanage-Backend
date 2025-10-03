package com.AIT.Optimanage.Controllers.dto;

import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OrganizationRequest {

    @NotBlank
    private String cnpj;

    @NotBlank
    private String razaoSocial;

    @NotBlank
    private String nomeFantasia;

    private String telefone;

    private String email;

    @NotNull
    private Boolean permiteOrcamento;

    @NotNull
    private LocalDate dataAssinatura;

    @NotNull
    private Integer planoId;

    private LocalDate trialInicio;

    private LocalDate trialFim;

    private TrialType trialTipo;

    @Valid
    @NotNull
    private UserRequest owner;
}
