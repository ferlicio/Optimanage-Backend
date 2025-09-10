package com.AIT.Optimanage.Models.Compra.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraProdutoResponseDTO {
    private Integer id;
    private Integer produtoId;
    private BigDecimal valorUnitario;
    private Integer quantidade;
    private BigDecimal valorTotal;
}
