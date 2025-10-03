package com.AIT.Optimanage.Analytics.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformResumoDTO {
    private BigDecimal volumeTotalVendas;
    private BigDecimal volumeTotalCompras;
    private BigDecimal lucroAgregado;
    private BigDecimal ticketMedio;
    private BigDecimal crescimentoMensal;
    private BigDecimal receitaRecorrenteMensal;
    private BigDecimal receitaRecorrenteAnual;
    private List<PlanoReceitaDTO> receitaPorPlano;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlanoReceitaDTO {
        private Integer planoId;
        private String planoNome;
        private long quantidadeOrganizacoes;
        private BigDecimal receitaRecorrenteMensal;
        private BigDecimal receitaRecorrenteAnual;
    }
}
