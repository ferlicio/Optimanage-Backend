package com.AIT.Optimanage.Analytics.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformResumoDTO {
    private BigDecimal volumeTotalVendas;
    private BigDecimal volumeTotalCompras;
    private BigDecimal lucroAgregado;
    private BigDecimal ticketMedio;
    private BigDecimal crescimentoMensal;
}
