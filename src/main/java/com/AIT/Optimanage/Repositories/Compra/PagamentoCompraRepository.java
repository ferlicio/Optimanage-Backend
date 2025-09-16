package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagamentoCompraRepository extends JpaRepository<CompraPagamento, Integer> {
    Optional<CompraPagamento> findByIdAndCompraOrganizationId(Integer idPagamento, Integer organizationId);

    Optional<CompraPagamento> findByIdAndCompraIdAndCompraOrganizationId(Integer id, Integer compraId, Integer organizationId);

    List<CompraPagamento> findAllByCompraIdAndCompraOrganizationId(Integer idCompra, Integer organizationId);

    List<CompraPagamento> findAllByCompraIdAndCompraOrganizationIdAndStatusPagamento(Integer idCompra, Integer organizationId, StatusPagamento statusPagamento);

    List<CompraPagamento> findAllByCompraOrganizationIdAndStatusPagamentoAndDataVencimentoAfter(Integer organizationId, StatusPagamento statusPagamento, LocalDate dataVencimento);

}
