package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

    @Query("SELECT c FROM Compra c " +
            "LEFT JOIN FETCH c.compraProdutos vp " +
            "LEFT JOIN FETCH vp.produto p " +
            "LEFT JOIN FETCH c.compraServicos vs " +
            "LEFT JOIN FETCH vs.servico s " +
            "LEFT JOIN c.pagamentos pag " +
            "WHERE (:id IS NULL OR c.sequencialUsuario = :id) " +
            "AND (:fornecedorId IS NULL OR c.fornecedor.id = :fornecedorId) " +
            "AND (:dataInicial IS NULL OR c.dataEfetuacao >= :dataInicial) " +
            "AND (:dataFinal IS NULL OR c.dataEfetuacao <= :dataFinal) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:pago IS NULL OR (CASE WHEN c.valorPendente <= 0 THEN TRUE ELSE FALSE END) = :pago) " +
            "AND (:formaPagamento IS NULL OR EXISTS (SELECT 1 FROM CompraPagamento pagSub WHERE pagSub.compra.id = c.id AND pagSub.formaPagamento = :formaPagamento))")
    Page<Compra> buscarCompras(
            @Param("id") Integer id,
            @Param("fornecedorId") Integer fornecedorId,
            @Param("dataInicial") String dataInicial,
            @Param("dataFinal") String dataFinal,
            @Param("status") StatusCompra status,
            @Param("pago") Boolean pago,
            @Param("formaPagamento") FormaPagamento formaPagamento,
            Pageable pageable
    );
}

