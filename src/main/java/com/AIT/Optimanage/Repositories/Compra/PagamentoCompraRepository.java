package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoCompraRepository extends JpaRepository<CompraPagamento, Integer> {

    Optional<CompraPagamento> findByIdAndCompraOrganizationId(Integer idPagamento, Integer organizationId);

    Optional<CompraPagamento> findByIdAndCompraIdAndCompraOrganizationId(Integer id, Integer compraId, Integer organizationId);

    List<CompraPagamento> findAllByCompraIdAndCompraOrganizationId(Integer idCompra, Integer organizationId);

    List<CompraPagamento> findAllByCompraIdAndCompraOrganizationIdAndStatusPagamento(Integer idCompra, Integer organizationId,
            StatusPagamento statusPagamento);

    List<CompraPagamento> findAllByCompraOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(Integer organizationId,
            StatusPagamento statusPagamento, LocalDate dataVencimento);

    @Query("""
            SELECT DISTINCT pagamento FROM CompraPagamento pagamento
            JOIN FETCH pagamento.compra compra
            LEFT JOIN FETCH compra.fornecedor fornecedor
            WHERE compra.organizationId = :organizationId
              AND pagamento.statusPagamento IN :statuses
              AND (:startDate IS NULL OR (
                    CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                          AND pagamento.dataPagamento IS NOT NULL
                         THEN pagamento.dataPagamento
                         ELSE pagamento.dataVencimento
                    END
                ) >= :startDate)
              AND (:endDate IS NULL OR (
                    CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                          AND pagamento.dataPagamento IS NOT NULL
                         THEN pagamento.dataPagamento
                         ELSE pagamento.dataVencimento
                    END
                ) <= :endDate)
              AND (
                    :statusFilter IS NULL
                    OR (
                        :statusFilter = com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus.CANCELLED
                        AND pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.ESTORNADO
                    )
                    OR (
                        :statusFilter = com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus.SCHEDULED
                        AND pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PENDENTE
                        AND (
                            CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                                      AND pagamento.dataPagamento IS NOT NULL
                                 THEN pagamento.dataPagamento
                                 ELSE pagamento.dataVencimento
                            END
                        ) > :today
                    )
                    OR (
                        :statusFilter = com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus.ACTIVE
                        AND (
                            pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                            OR (
                                pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PENDENTE
                                AND (
                                    (
                                        CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                                                  AND pagamento.dataPagamento IS NOT NULL
                                             THEN pagamento.dataPagamento
                                             ELSE pagamento.dataVencimento
                                        END
                                    ) <= :today
                                    OR (
                                        CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                                                  AND pagamento.dataPagamento IS NOT NULL
                                             THEN pagamento.dataPagamento
                                             ELSE pagamento.dataVencimento
                                        END
                                    ) IS NULL
                                )
                            )
                        )
                    )
                )
            ORDER BY
                CASE WHEN :sortKey = 'MOVEMENT_DATE' AND :ascending = true THEN (
                    CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                          AND pagamento.dataPagamento IS NOT NULL
                         THEN pagamento.dataPagamento
                         ELSE pagamento.dataVencimento
                    END
                ) END ASC,
                CASE WHEN :sortKey = 'MOVEMENT_DATE' AND :ascending = false THEN (
                    CASE WHEN pagamento.statusPagamento = com.AIT.Optimanage.Models.Enums.StatusPagamento.PAGO
                              AND pagamento.dataPagamento IS NOT NULL
                         THEN pagamento.dataPagamento
                         ELSE pagamento.dataVencimento
                    END
                ) END DESC,
                CASE WHEN :sortKey = 'AMOUNT' AND :ascending = true THEN pagamento.valorPago END ASC,
                CASE WHEN :sortKey = 'AMOUNT' AND :ascending = false THEN pagamento.valorPago END DESC,
                CASE WHEN :sortKey = 'DESCRIPTION' AND :ascending = true THEN (
                    CONCAT(
                        'Compra ',
                        COALESCE(CONCAT('#', CAST(compra.sequencialUsuario AS string)), 'Compra'),
                        CASE WHEN fornecedor.nome IS NOT NULL AND fornecedor.nome <> ''
                             THEN CONCAT(' - ', fornecedor.nome)
                             ELSE ''
                        END
                    )
                ) END ASC,
                CASE WHEN :sortKey = 'DESCRIPTION' AND :ascending = false THEN (
                    CONCAT(
                        'Compra ',
                        COALESCE(CONCAT('#', CAST(compra.sequencialUsuario AS string)), 'Compra'),
                        CASE WHEN fornecedor.nome IS NOT NULL AND fornecedor.nome <> ''
                             THEN CONCAT(' - ', fornecedor.nome)
                             ELSE ''
                        END
                    )
                ) END DESC,
                CASE WHEN :sortKey = 'CREATED_AT' AND :ascending = true THEN pagamento.createdAt END ASC,
                CASE WHEN :sortKey = 'CREATED_AT' AND :ascending = false THEN pagamento.createdAt END DESC,
                pagamento.id ASC
            """)
    Page<CompraPagamento> findInstallmentsByOrganizationAndStatusesAndDateRange(
            @Param("organizationId") Integer organizationId,
            @Param("statuses") Collection<StatusPagamento> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("ascending") boolean ascending,
            @Param("sortKey") String sortKey,
            @Param("statusFilter") CashFlowStatus statusFilter,
            @Param("today") LocalDate today,
            Pageable pageable);
}
