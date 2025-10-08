package com.AIT.Optimanage.Models.Compra.DTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraServicoDTO {
    @NotNull
    private Integer servicoId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @DecimalMin(value = "0.0", inclusive = false, message = "O valor unitário deve ser maior que zero")
    private BigDecimal valorUnitario;

    public CompraServicoDTO(Integer servicoId, Integer quantidade) {
        this.servicoId = servicoId;
        this.quantidade = quantidade;
    }
}
