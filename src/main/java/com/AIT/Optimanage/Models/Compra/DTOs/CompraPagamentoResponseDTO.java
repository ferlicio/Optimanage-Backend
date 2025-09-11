package com.AIT.Optimanage.Models.Compra.DTOs;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
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
public class CompraPagamentoResponseDTO {
    private Integer id;
    private BigDecimal valorPago;
    private LocalDate dataPagamento;
    private LocalDate dataVencimento;
    private FormaPagamento formaPagamento;
    private StatusPagamento statusPagamento;
    private String observacoes;
}
