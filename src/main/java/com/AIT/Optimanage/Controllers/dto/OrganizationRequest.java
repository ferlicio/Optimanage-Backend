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

    @Valid
    @NotNull
    private UserRequest owner;
}
