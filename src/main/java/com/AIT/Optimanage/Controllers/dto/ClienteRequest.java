package com.AIT.Optimanage.Controllers.dto;

import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @NotNull
    private Integer atividadeId;

    @NotNull
    private TipoPessoa tipoPessoa;

    @NotBlank
    private String origem;

    private Boolean ativo;

    @Size(max = 64)
    private String nome;

    @Size(max = 55)
    private String nomeFantasia;

    @Size(max = 64)
    private String razaoSocial;

    @Size(max = 14)
    private String cpf;

    @Size(max = 18)
    private String cnpj;

    @Size(max = 18)
    private String inscricaoEstadual;

    @Size(max = 18)
    private String inscricaoMunicipal;

    @Size(max = 64)
    private String site;

    @Size(max = 256)
    private String informacoesAdicionais;
}
