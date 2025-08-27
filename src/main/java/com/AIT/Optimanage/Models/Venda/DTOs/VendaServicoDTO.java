package com.AIT.Optimanage.Models.Venda.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaServicoDTO {
    @NotNull
    private Integer servicoId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    private BigDecimal desconto = BigDecimal.ZERO;
}
