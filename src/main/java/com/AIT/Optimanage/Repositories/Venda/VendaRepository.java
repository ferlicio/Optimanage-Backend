package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

    @Query("SELECT v FROM Venda v " +
            "LEFT JOIN FETCH v.vendaProdutos vp " +
            "LEFT JOIN FETCH vp.produto p " +
            "LEFT JOIN FETCH v.vendaServicos vs " +
            "LEFT JOIN FETCH vs.servico s " +
            "LEFT JOIN v.pagamentos pag " +
            "WHERE " +
            "((:id IS NOT NULL AND v.organizationId = :organizationId AND v.sequencialUsuario = :id) " +
            "OR (:organizationId IS NULL OR v.organizationId = :organizationId)) " +
            "AND (:clienteId IS NULL OR v.cliente.id = :clienteId) " +
            "AND (:dataInicial IS NULL OR v.dataEfetuacao >= :dataInicial) " +
            "AND (:dataFinal IS NULL OR v.dataEfetuacao <= :dataFinal) " +
            "AND (:status IS NULL OR v.status = :status) " +
            "AND (:pago IS NULL OR (CASE WHEN v.valorPendente <= 0 THEN TRUE ELSE FALSE END) = :pago) " +
            "AND (:formaPagamento IS NULL OR EXISTS (SELECT 1 FROM VendaPagamento pagSub WHERE pagSub.venda.id = v.id AND pagSub.formaPagamento = :formaPagamento))")
    Page<Venda> buscarVendas(
            @Param("organizationId") Integer organizationId,
            @Param("id") Integer id,
            @Param("clienteId") Integer clienteId,
            @Param("dataInicial") String dataInicial,
            @Param("dataFinal") String dataFinal,
            @Param("status") StatusVenda status,
            @Param("pago") Boolean pago,
            @Param("formaPagamento") FormaPagamento formaPagamento,
            Pageable pageable
    );

    Optional<Venda> findByIdAndOrganizationId(Integer idVenda, Integer organizationId);

    @Query("SELECT vp.produto.id AS produtoId, SUM(vp.quantidade) AS totalQuantidade " +
            "FROM Venda v JOIN v.vendaProdutos vp " +
            "WHERE v.cliente.id = :clienteId AND v.organizationId = :organizationId " +
            "GROUP BY vp.produto.id ORDER BY totalQuantidade DESC")
    List<Object[]> findTopProdutosByCliente(@Param("clienteId") Integer clienteId,
                                            @Param("organizationId") Integer organizationId);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "JOIN FETCH v.vendaProdutos vp " +
            "JOIN FETCH vp.produto p " +
            "WHERE v.organizationId = :organizationId")
    List<Venda> findAllWithProdutosByOrganization(@Param("organizationId") Integer organizationId);
}
