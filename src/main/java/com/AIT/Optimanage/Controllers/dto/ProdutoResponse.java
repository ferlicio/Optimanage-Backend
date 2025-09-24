package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponse {

    private Integer id;
    private Integer organizationId;
    private Integer fornecedorId;
    private Integer sequencialUsuario;
    private String codigoReferencia;
    private String nome;
    private String descricao;
    private BigDecimal custo;
    private Boolean disponivelVenda;
    private BigDecimal valorVenda;
    private Integer qtdEstoque;
    private Boolean terceirizado;
    private Boolean ativo;
    private Integer estoqueMinimo;
    private Integer prazoReposicaoDias;
}
