package com.AIT.Optimanage.Controllers.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoRequest {

    @NotBlank
    private String nome;

    @NotNull
    @PositiveOrZero
    private Float valor;

    @NotNull
    @Positive
    private Integer duracaoDias;

    @NotNull
    @Positive
    private Integer qtdAcessos;

    @NotNull
    @PositiveOrZero
    private Integer maxUsuarios;

    @NotNull
    @PositiveOrZero
    private Integer maxProdutos;

    @NotNull
    @PositiveOrZero
    private Integer maxClientes;

    @NotNull
    @PositiveOrZero
    private Integer maxFornecedores;

    @NotNull
    @PositiveOrZero
    private Integer maxServicos;

    @NotNull
    private Boolean agendaHabilitada;

    @NotNull
    private Boolean recomendacoesHabilitadas;

    @NotNull
    private Boolean pagamentosHabilitados;

    @NotNull
    private Boolean suportePrioritario;
}

