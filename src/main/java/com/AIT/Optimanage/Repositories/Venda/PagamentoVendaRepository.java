package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
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
public interface PagamentoVendaRepository extends JpaRepository<VendaPagamento, Integer> {

    List<VendaPagamento> findAllByVendaIdAndVendaOrganizationId(Integer idVenda, Integer organizationId);

    List<VendaPagamento> findAllByVendaIdAndVendaOrganizationIdAndStatusPagamento(Integer idVenda, Integer organizationId,
            StatusPagamento statusPagamento);

    Optional<VendaPagamento> findByIdAndVendaOrganizationId(Integer idPagamento, Integer organizationId);

    Optional<VendaPagamento> findByIdAndVendaAndVendaOrganizationId(Integer idPagamento, Venda venda, Integer organizationId);

    List<VendaPagamento> findAllByVendaOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(Integer organizationId,
            StatusPagamento statusPagamento, LocalDate dataVencimento);

    @Query("""
            SELECT DISTINCT pagamento FROM VendaPagamento pagamento
            JOIN FETCH pagamento.venda venda
            LEFT JOIN FETCH venda.cliente cliente
            WHERE venda.organizationId = :organizationId
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
                        'Venda ',
                        COALESCE(CONCAT('#', CAST(venda.sequencialUsuario AS string)), 'Venda'),
                        CASE WHEN cliente.nome IS NOT NULL AND cliente.nome <> ''
                             THEN CONCAT(' - ', cliente.nome)
                             ELSE ''
                        END
                    )
                ) END ASC,
                CASE WHEN :sortKey = 'DESCRIPTION' AND :ascending = false THEN (
                    CONCAT(
                        'Venda ',
                        COALESCE(CONCAT('#', CAST(venda.sequencialUsuario AS string)), 'Venda'),
                        CASE WHEN cliente.nome IS NOT NULL AND cliente.nome <> ''
                             THEN CONCAT(' - ', cliente.nome)
                             ELSE ''
                        END
                    )
                ) END DESC,
                CASE WHEN :sortKey = 'CREATED_AT' AND :ascending = true THEN pagamento.createdAt END ASC,
                CASE WHEN :sortKey = 'CREATED_AT' AND :ascending = false THEN pagamento.createdAt END DESC,
                pagamento.id ASC
            """)
    Page<VendaPagamento> findInstallmentsByOrganizationAndStatusesAndDateRange(
            @Param("organizationId") Integer organizationId,
            @Param("statuses") Collection<StatusPagamento> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("ascending") boolean ascending,
            @Param("sortKey") String sortKey,
            Pageable pageable);
}
