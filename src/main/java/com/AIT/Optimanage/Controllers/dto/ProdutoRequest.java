package com.AIT.Optimanage.Controllers.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequest {

    private Integer fornecedorId;

    @NotNull
    private Integer sequencialUsuario;

    @NotBlank
    private String codigoReferencia;

    @NotBlank
    private String nome;

    private String descricao;

    @NotNull
    @PositiveOrZero
    private BigDecimal custo;

    private Boolean disponivelVenda;

    @NotNull
    @PositiveOrZero
    private BigDecimal valorVenda;

    @NotNull
    private Integer qtdEstoque;

    private Boolean terceirizado;
    private Boolean ativo;

    @PositiveOrZero
    private Integer estoqueMinimo;

    @PositiveOrZero
    private Integer prazoReposicaoDias;
}
