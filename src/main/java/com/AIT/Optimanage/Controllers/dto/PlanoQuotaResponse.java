package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoQuotaResponse {

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
    private Integer usuariosUtilizados;
    private Integer usuariosRestantes;
    private Integer produtosUtilizados;
    private Integer produtosRestantes;
    private Integer clientesUtilizados;
    private Integer clientesRestantes;
    private Integer fornecedoresUtilizados;
    private Integer fornecedoresRestantes;
    private Integer servicosUtilizados;
    private Integer servicosRestantes;
}
