package com.AIT.Optimanage.Models.Venda.DTOs;

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
public class VendaServicoResponseDTO {
    private Integer id;
    @JsonProperty("servico_id")
    private Integer servicoId;
    private BigDecimal valorUnitario;
    private Integer quantidade;
    private BigDecimal desconto;
    private BigDecimal valorFinal;
}
