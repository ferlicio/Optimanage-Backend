package com.AIT.Optimanage.Analytics.DTOs;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PlatformOnboardingMetricsDTO {

    long totalOrganizacoes;
    long totalOrganizacoesAssinadas;
    BigDecimal tempoMedioDiasAteAssinatura;
    BigDecimal percentualAssinatura7Dias;
    BigDecimal percentualAssinatura30Dias;
    BigDecimal taxaConversaoTotal;
    BigDecimal taxaConversaoUltimos30Dias;
    long totalTrials;
    long trialsAtivos;
    long trialsExpirados;
    BigDecimal taxaConversaoTrials;
    BigDecimal taxaConversaoTrialsNoPrazo;
}
