package com.AIT.Optimanage.Models.Venda.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaProdutoDTO {
    @NotNull
    private Integer produtoId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    private BigDecimal desconto = BigDecimal.ZERO;
}