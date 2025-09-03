package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<VendaPagamento, Integer> {
    List<VendaPagamento> findAllByVendaIdAndVendaOwnerUser(Integer idVenda, User loggedUser);

    List<VendaPagamento> findAllByVendaIdAndVendaOwnerUserAndStatusPagamento(Integer idVenda, User loggedUser, StatusPagamento statusPagamento);

    Optional<VendaPagamento> findByIdAndVendaOwnerUser(Integer idPagamento, User loggedUser);

    Optional<VendaPagamento> findByIdAndVendaAndVendaOwnerUser(Integer idPagamento, Venda venda, User loggedUser);
}
