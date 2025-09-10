package com.AIT.Optimanage.Models.Compra.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("produto_id")
    private Integer produtoId;
    private BigDecimal valorUnitario;
    private Integer quantidade;
    private BigDecimal valorTotal;
}
