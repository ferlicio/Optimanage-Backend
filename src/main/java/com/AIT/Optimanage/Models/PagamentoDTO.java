package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valorPago;
    @NotNull
    private LocalDate dataPagamento;
    @NotNull
    private FormaPagamento formaPagamento;
    @NotNull
    private StatusPagamento statusPagamento;
    private String condicaoPagamento;
    private String observacoes;
}
