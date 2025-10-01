package com.AIT.Optimanage.Analytics.DTOs;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PlatformHealthScoreDTO {

    long totalOrganizations;
    long clientesSemVendasRecentes;
    BigDecimal volumeComprasUltimos30Dias;

    HealthSegment ativoEmVendas;
    HealthSegment ativoEmCompras;
    HealthSegment emRisco;
    HealthSegment churn;

    @Value
    @Builder
    public static class HealthSegment {
        long organizations;
        BigDecimal percentage;
    }
}
