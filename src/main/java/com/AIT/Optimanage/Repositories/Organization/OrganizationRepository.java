package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("update Organization o set o.organizationId = :organizationId where o.id = :organizationId")
    void updateOrganizationTenant(@Param("organizationId") Integer organizationId);

    @Query("""
            SELECT p.id AS planId,
                   p.nome AS planName,
                   COUNT(o) AS totalOrganizations,
                   SUM(CASE WHEN p.agendaHabilitada = true THEN 1 ELSE 0 END) AS agendaEnabledCount,
                   SUM(CASE WHEN p.recomendacoesHabilitadas = true THEN 1 ELSE 0 END) AS recomendacoesEnabledCount,
                   SUM(CASE WHEN p.pagamentosHabilitados = true THEN 1 ELSE 0 END) AS pagamentosEnabledCount,
                   SUM(CASE WHEN p.suportePrioritario = true THEN 1 ELSE 0 END) AS suportePrioritarioEnabledCount,
                   SUM(CASE WHEN p.monitoramentoEstoqueHabilitado = true THEN 1 ELSE 0 END) AS monitoramentoEstoqueEnabledCount,
                   SUM(CASE WHEN p.metricasProdutoHabilitadas = true THEN 1 ELSE 0 END) AS metricasProdutoEnabledCount,
                   SUM(CASE WHEN p.integracaoMarketplaceHabilitada = true THEN 1 ELSE 0 END) AS integracaoMarketplaceEnabledCount
            FROM Organization o
            JOIN o.planoAtivoId p
            GROUP BY p.id, p.nome
            """)
    List<PlanFeatureAdoptionProjection> aggregateFeatureAdoptionByPlan();
}

