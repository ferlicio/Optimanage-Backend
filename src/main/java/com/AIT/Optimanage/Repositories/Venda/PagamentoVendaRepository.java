package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
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
            """)
    List<VendaPagamento> findInstallmentsByOrganizationAndStatusesAndDateRange(
            @Param("organizationId") Integer organizationId,
            @Param("statuses") Collection<StatusPagamento> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
