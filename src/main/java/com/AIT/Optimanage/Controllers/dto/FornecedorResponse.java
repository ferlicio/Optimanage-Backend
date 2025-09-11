package com.AIT.Optimanage.Controllers.dto;

import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FornecedorResponse {
    private Integer id;
    private Integer ownerUserId;
    private Integer atividadeId;
    private LocalDate dataCadastro;
    private TipoPessoa tipoPessoa;
    private String origem;
    private Boolean ativo;
    private String nome;
    private String nomeFantasia;
    private String razaoSocial;
    private String cpf;
    private String cnpj;
    private String inscricaoEstadual;
    private String inscricaoMunicipal;
    private String site;
    private String informacoesAdicionais;
}
