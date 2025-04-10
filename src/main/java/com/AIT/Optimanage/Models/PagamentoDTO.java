package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {
    @NotNull
    private Double valorPago;
    @NotNull
    private LocalDate dataPagamento;
    @NotNull
    private FormaPagamento formaPagamento;
    @NotNull
    private StatusPagamento statusPagamento;
    private String condicaoPagamento;
    private String observacoes;
}
