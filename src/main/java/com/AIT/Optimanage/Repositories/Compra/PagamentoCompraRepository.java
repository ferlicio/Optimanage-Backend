package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
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
            """)
    List<CompraPagamento> findInstallmentsByOrganizationAndStatusesAndDateRange(
            @Param("organizationId") Integer organizationId,
            @Param("statuses") Collection<StatusPagamento> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
