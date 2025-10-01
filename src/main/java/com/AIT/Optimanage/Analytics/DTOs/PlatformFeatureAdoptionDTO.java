package com.AIT.Optimanage.Analytics.DTOs;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PlatformFeatureAdoptionDTO {

    long totalOrganizations;

    FeatureAdoptionMetrics agenda;

    FeatureAdoptionMetrics recomendacoes;

    FeatureAdoptionMetrics pagamentos;

    FeatureAdoptionMetrics suportePrioritario;

    FeatureAdoptionMetrics monitoramentoEstoque;

    FeatureAdoptionMetrics metricasProduto;

    FeatureAdoptionMetrics integracaoMarketplace;

    @Value
    @Builder
    public static class FeatureAdoptionMetrics {
        long organizations;
        BigDecimal adoptionPercentage;
    }
}
