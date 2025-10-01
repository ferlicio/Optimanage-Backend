package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Integer>, JpaSpecificationExecutor<Compra> {
    Optional<Compra> findByIdAndOrganizationId(Integer idCompra, Integer organizationId);

    @Query("""
            SELECT c FROM Compra c
            WHERE c.organizationId = :organizationId
              AND c.createdBy = :userId
              AND c.dataAgendada IS NOT NULL
              AND (:inicio IS NULL OR c.dataAgendada >= :inicio)
              AND (:fim IS NULL OR c.dataAgendada <= :fim)
            """)
    List<Compra> findAgendadasNoPeriodo(@Param("organizationId") Integer organizationId,
                                        @Param("userId") Integer userId,
                                        @Param("inicio") LocalDate inicio,
                                        @Param("fim") LocalDate fim);

    @Query("SELECT SUM(c.valorFinal) FROM Compra c WHERE c.organizationId = :organizationId")
    BigDecimal sumValorFinalByOrganization(@Param("organizationId") Integer organizationId);

    @Query("SELECT SUM(c.valorFinal) FROM Compra c WHERE c.organizationId = :organizationId AND c.status = :status")
    BigDecimal sumValorFinalByOrganizationAndStatus(@Param("organizationId") Integer organizationId,
                                                    @Param("status") StatusCompra status);

    @Query("""
            SELECT COALESCE(SUM(c.valorFinal), 0)
            FROM Compra c
            WHERE (:organizationId IS NULL OR c.organizationId = :organizationId)
            """)
    BigDecimal sumValorFinalGlobal(@Param("organizationId") Integer organizationId);

    @Query("""
            SELECT c FROM Compra c
            WHERE c.fornecedor.id = :fornecedorId
              AND c.organizationId = :organizationId
              AND c.status IN :statuses
            """)
    List<Compra> findByFornecedorIdAndOrganizationIdAndStatusIn(@Param("fornecedorId") Integer fornecedorId,
                                                                @Param("organizationId") Integer organizationId,
                                                                @Param("statuses") List<StatusCompra> statuses);

    @Query("""
            SELECT c.dataEfetuacao AS dia,
                   COUNT(DISTINCT c.organizationId) AS quantidade
            FROM Compra c
            WHERE c.dataEfetuacao IS NOT NULL
              AND c.dataEfetuacao BETWEEN :inicio AND :fim
            GROUP BY c.dataEfetuacao
            ORDER BY c.dataEfetuacao
            """)
    List<Object[]> countDistinctOrganizationsWithPurchasesByDate(@Param("inicio") LocalDate inicio,
                                                                  @Param("fim") LocalDate fim);

    @Query("""
            SELECT DISTINCT c.organizationId
            FROM Compra c
            WHERE c.dataEfetuacao IS NOT NULL
              AND c.dataEfetuacao BETWEEN :inicio AND :fim
            """)
    List<Integer> findDistinctOrganizationIdsWithPurchasesBetween(@Param("inicio") LocalDate inicio,
                                                                   @Param("fim") LocalDate fim);

    @Query("""
            SELECT COUNT(DISTINCT c.organizationId)
            FROM Compra c
            WHERE c.dataEfetuacao IS NOT NULL
              AND c.dataEfetuacao BETWEEN :inicio AND :fim
              AND (:excludedOrganizationId IS NULL OR c.organizationId <> :excludedOrganizationId)
            """)
    long countDistinctOrganizationsWithPurchasesBetween(@Param("inicio") LocalDate inicio,
                                                         @Param("fim") LocalDate fim,
                                                         @Param("excludedOrganizationId") Integer excludedOrganizationId);

    @Query("""
            SELECT COALESCE(SUM(c.valorFinal), 0)
            FROM Compra c
            WHERE c.dataEfetuacao BETWEEN :inicio AND :fim
              AND (:excludedOrganizationId IS NULL OR c.organizationId <> :excludedOrganizationId)
            """)
    BigDecimal sumValorFinalGlobalBetweenDates(@Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim,
                                               @Param("excludedOrganizationId") Integer excludedOrganizationId);
}
