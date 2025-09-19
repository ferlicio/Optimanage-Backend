package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<VendaPagamento, Integer> {
    List<VendaPagamento> findAllByVendaIdAndVendaOrganizationId(Integer idVenda, Integer organizationId);

    List<VendaPagamento> findAllByVendaIdAndVendaOrganizationIdAndStatusPagamento(Integer idVenda, Integer organizationId, StatusPagamento statusPagamento);

    Optional<VendaPagamento> findByIdAndVendaOrganizationId(Integer idPagamento, Integer organizationId);

    Optional<VendaPagamento> findByIdAndVendaAndVendaOrganizationId(Integer idPagamento, Venda venda, Integer organizationId);

    List<VendaPagamento> findAllByVendaOrganizationIdAndStatusPagamentoAndDataVencimentoAfter(Integer organizationId, StatusPagamento statusPagamento, LocalDate dataVencimento);
}
