package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoResponse {

    private Integer id;
    private String nome;
    private Float valor;
    private Integer duracaoDias;
    private Integer qtdAcessos;
    private Integer maxUsuarios;
    private Integer maxProdutos;
    private Integer maxClientes;
    private Integer maxFornecedores;
    private Integer maxServicos;
    private Boolean agendaHabilitada;
    private Boolean recomendacoesHabilitadas;
    private Boolean pagamentosHabilitados;
    private Boolean suportePrioritario;
    private Boolean monitoramentoEstoqueHabilitado;
    private Boolean metricasProdutoHabilitadas;
    private Boolean integracaoMarketplaceHabilitada;
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

