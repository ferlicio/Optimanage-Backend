package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<VendaPagamento, Integer> {
    List<VendaPagamento> findAllByVenda(Venda venda);

    List<VendaPagamento> findAllByVendaAndStatusPagamento(Venda venda, StatusPagamento statusPagamento);

    Optional<VendaPagamento> findByIdAndVenda(Integer idPagamento, Venda venda);
}
