package com.AIT.Optimanage.Models.Venda.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaProdutoDTO {
    @NotNull
    private Integer produtoId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    private Double desconto = 0.0;
}