package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoCompraRepository extends JpaRepository<CompraPagamento, Integer> {
    Optional<CompraPagamento> findByIdAndCompra(Integer id, Compra compra);

    List<CompraPagamento> findAllByCompra(Compra compra);

    List<CompraPagamento> findAllByCompraAndStatusPagamento(Compra compra, StatusPagamento statusPagamento);

}
