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
public class ServicoRequest {

    private Integer fornecedorId;

    @NotNull
    private Integer sequencialUsuario;

    @NotBlank
    private String nome;

    private String descricao;

    @NotNull
    @PositiveOrZero
    private Double custo;

    private Boolean disponivelVenda;

    @NotNull
    @PositiveOrZero
    private Double valorVenda;

    @NotNull
    private Integer tempoExecucao;

    private Boolean terceirizado;
    private Boolean ativo;
}
