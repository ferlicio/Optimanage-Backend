package com.AIT.Optimanage.Models.Compra.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraProdutoDTO {
    @NotNull
    private Integer produtoId;

    @NotNull
    @Min(1)
    private Integer quantidade;
}
