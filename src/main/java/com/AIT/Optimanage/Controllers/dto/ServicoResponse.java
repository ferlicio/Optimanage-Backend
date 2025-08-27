package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponse {

    private Integer id;
    private Integer ownerUserId;
    private Integer fornecedorId;
    private Integer sequencialUsuario;
    private String nome;
    private String descricao;
    private Double custo;
    private Boolean disponivelVenda;
    private Double valorVenda;
    private Integer tempoExecucao;
    private Boolean terceirizado;
    private Boolean ativo;
}

