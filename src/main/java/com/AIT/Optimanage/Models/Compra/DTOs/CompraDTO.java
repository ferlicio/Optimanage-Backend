package com.AIT.Optimanage.Models.Compra.DTOs;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDTO {
    @NotNull
    private Integer fornecedorId;
    @NotNull
    private LocalDate dataEfetuacao = LocalDate.now();
    private LocalDate dataAgendada = null;
    private LocalTime horaAgendada;
    private Duration duracaoEstimada = Duration.ofHours(1);
    private LocalDate dataCobranca;
    @DecimalMin(value = "0.0")
    private BigDecimal valorFinal;
    private String condicaoPagamento;
    @NotNull
    private StatusCompra status = StatusCompra.ORCAMENTO;
    private String observacoes;
    @NotNull
    private List<@Valid CompraProdutoDTO> produtos;
    @NotNull
    private List<@Valid CompraServicoDTO> servicos;

    public boolean hasNoItems() {
        return produtos.isEmpty() && servicos.isEmpty();
    }
}
