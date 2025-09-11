package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagamentoCompraRepository extends JpaRepository<CompraPagamento, Integer> {
    Optional<CompraPagamento> findByIdAndCompraOwnerUser(Integer idPagamento, User loggedUser);

    Optional<CompraPagamento> findByIdAndCompraAndCompraOwnerUser(Integer id, Compra compra, User loggedUser);

    List<CompraPagamento> findAllByCompraIdAndCompraOwnerUser(Integer idCompra, User loggedUser);

    List<CompraPagamento> findAllByCompraIdAndCompraOwnerUserAndStatusPagamento(Integer idCompra, User loggedUser, StatusPagamento statusPagamento);

    List<CompraPagamento> findAllByCompraOwnerUserAndStatusPagamentoAndDataVencimentoAfter(User loggedUser, StatusPagamento statusPagamento, LocalDate dataVencimento);

}
