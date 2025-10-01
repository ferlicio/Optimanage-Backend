package com.AIT.Optimanage.Analytics.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformEngajamentoDTO {
    private long organizacoesAtivas30Dias;
    private long organizacoesAtivas60Dias;
    private long organizacoesInativas60Dias;
    private BigDecimal tempoMedioPrimeiraVendaDias;
    private BigDecimal taxaRetencao30Dias;
    private BigDecimal taxaRetencao60Dias;
}
