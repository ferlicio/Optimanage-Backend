package com.AIT.Optimanage.Repositories.Organization;

public interface PlanFeatureAdoptionProjection {

    Integer getPlanId();

    String getPlanName();

    Long getTotalOrganizations();

    Long getAgendaEnabledCount();

    Long getRecomendacoesEnabledCount();

    Long getPagamentosEnabledCount();

    Long getSuportePrioritarioEnabledCount();

    Long getMonitoramentoEstoqueEnabledCount();

    Long getMetricasProdutoEnabledCount();

    Long getIntegracaoMarketplaceEnabledCount();
}
