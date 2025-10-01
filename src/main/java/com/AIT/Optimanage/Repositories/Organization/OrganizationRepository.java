package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("update Organization o set o.organizationId = :organizationId where o.id = :organizationId")
    void updateOrganizationTenant(@Param("organizationId") Integer organizationId);

    @Query("""
           SELECT COUNT(o)
            FROM Organization o
            WHERE (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
              AND NOT EXISTS (
                    SELECT 1
                    FROM Venda v
                    WHERE v.organizationId = o.id
                      AND v.dataEfetuacao >= :cutoff
              )
            """)
    long countOrganizationsWithoutSalesSince(@Param("cutoff") LocalDate cutoff,
                                             @Param("excludedOrganizationId") Integer excludedOrganizationId);
    @Query("""
           SELECT p.id AS planId,
                   p.nome AS planName,
                   COUNT(o) AS totalOrganizations,
                   COALESCE(SUM(CASE WHEN p.agendaHabilitada = true THEN 1 ELSE 0 END), 0) AS agendaEnabledCount,
                   COALESCE(SUM(CASE WHEN p.recomendacoesHabilitadas = true THEN 1 ELSE 0 END), 0) AS recomendacoesEnabledCount,
                   COALESCE(SUM(CASE WHEN p.pagamentosHabilitados = true THEN 1 ELSE 0 END), 0) AS pagamentosEnabledCount,
                   COALESCE(SUM(CASE WHEN p.suportePrioritario = true THEN 1 ELSE 0 END), 0) AS suportePrioritarioEnabledCount,
                   COALESCE(SUM(CASE WHEN p.monitoramentoEstoqueHabilitado = true THEN 1 ELSE 0 END), 0) AS monitoramentoEstoqueEnabledCount,
                   COALESCE(SUM(CASE WHEN p.metricasProdutoHabilitadas = true THEN 1 ELSE 0 END), 0) AS metricasProdutoEnabledCount,
                   COALESCE(SUM(CASE WHEN p.integracaoMarketplaceHabilitada = true THEN 1 ELSE 0 END), 0) AS integracaoMarketplaceEnabledCount
            FROM Organization o
            JOIN o.planoAtivoId p
            WHERE (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
            GROUP BY p.id, p.nome
            """)
    List<PlanFeatureAdoptionProjection> aggregateFeatureAdoptionByPlan(@Param("excludedOrganizationId") Integer excludedOrganizationId);

    @Query("""
            SELECT function('DATE', o.createdAt) AS dia,
                   COUNT(o) AS quantidade
            FROM Organization o
            WHERE o.createdAt IS NOT NULL
              AND o.createdAt BETWEEN :inicio AND :fim
              AND (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
            GROUP BY function('DATE', o.createdAt)
            ORDER BY dia
            """)
    List<Object[]> countOrganizationsCreatedByDateRange(@Param("inicio") LocalDateTime inicio,
                                                         @Param("fim") LocalDateTime fim,
                                                         @Param("excludedOrganizationId") Integer excludedOrganizationId);

    @Query("""
            SELECT o.dataAssinatura AS dia,
                   COUNT(o) AS quantidade
            FROM Organization o
            WHERE o.dataAssinatura IS NOT NULL
              AND o.dataAssinatura BETWEEN :inicio AND :fim
              AND (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
            GROUP BY o.dataAssinatura
            ORDER BY o.dataAssinatura
            """)
    List<Object[]> countOrganizationsSignedByDateRange(@Param("inicio") LocalDate inicio,
                                                        @Param("fim") LocalDate fim,
                                                        @Param("excludedOrganizationId") Integer excludedOrganizationId);

    @Query("""
            SELECT COUNT(o)
            FROM Organization o
            WHERE (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
            """)
    long countAllExcluding(@Param("excludedOrganizationId") Integer excludedOrganizationId);
}

