package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Venda.projection.ProdutoQuantidadeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer>, JpaSpecificationExecutor<Venda> {

    @Query("SELECT v FROM Venda v " +
            "WHERE " +
            "(:organizationId IS NULL OR v.organizationId = :organizationId) " +
            "AND (:id IS NULL OR v.sequencialUsuario = :id) " +
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
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("status") StatusVenda status,
            @Param("pago") Boolean pago,
            @Param("formaPagamento") FormaPagamento formaPagamento,
            Pageable pageable
    );

    Optional<Venda> findByIdAndOrganizationId(Integer idVenda, Integer organizationId);

    @EntityGraph(attributePaths = {
            "vendaProdutos",
            "vendaProdutos.produto",
            "vendaServicos",
            "vendaServicos.servico",
            "pagamentos"
    })
    Optional<Venda> findDetailedByIdAndOrganizationId(Integer idVenda, Integer organizationId);

    @Query("SELECT vp.produto.id AS produtoId, SUM(vp.quantidade) AS totalQuantidade " +
            "FROM Venda v JOIN v.vendaProdutos vp " +
            "WHERE v.cliente.id = :clienteId AND v.organizationId = :organizationId " +
            "GROUP BY vp.produto.id ORDER BY totalQuantidade DESC")
    List<Object[]> findTopProdutosByCliente(@Param("clienteId") Integer clienteId,
                                            @Param("organizationId") Integer organizationId);

    @Query("SELECT vp.produto.id AS produtoId, SUM(vp.quantidade) AS totalQuantidade " +
            "FROM Venda v JOIN v.vendaProdutos vp " +
            "WHERE v.organizationId = :organizationId " +
            "GROUP BY vp.produto.id ORDER BY totalQuantidade DESC")
    List<Object[]> findTopProdutosByOrganization(@Param("organizationId") Integer organizationId);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "JOIN FETCH v.vendaProdutos vp " +
            "JOIN FETCH vp.produto p " +
            "WHERE v.organizationId = :organizationId")
    List<Venda> findAllWithProdutosByOrganization(@Param("organizationId") Integer organizationId);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "LEFT JOIN FETCH v.vendaProdutos vp " +
            "LEFT JOIN FETCH vp.produto p " +
            "WHERE v.organizationId = :organizationId " +
            "AND (:cutoff IS NULL OR v.dataEfetuacao >= :cutoff)")
    List<Venda> findRecentWithItensByOrganization(@Param("organizationId") Integer organizationId,
                                                  @Param("cutoff") LocalDate cutoff);

    @Query("SELECT vp.produto.id AS produtoId, SUM(vp.quantidade) AS totalQuantidade " +
            "FROM Venda v JOIN v.vendaProdutos vp " +
            "WHERE v.organizationId = :organizationId " +
            "AND v.status = com.AIT.Optimanage.Models.Venda.Related.StatusVenda.CONCRETIZADA " +
            "AND v.dataEfetuacao BETWEEN :inicio AND :fim " +
            "GROUP BY vp.produto.id")
    List<ProdutoQuantidadeProjection> sumQuantidadeVendidaPorProduto(@Param("organizationId") Integer organizationId,
                                                                     @Param("inicio") LocalDate inicio,
                                                                     @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(v.valorFinal), 0) FROM Venda v " +
            "WHERE v.organizationId = :organizationId " +
            "AND v.cliente.id = :clienteId " +
            "AND v.status = :status")
    BigDecimal sumValorFinalByClienteAndStatus(@Param("organizationId") Integer organizationId,
                                               @Param("clienteId") Integer clienteId,
                                               @Param("status") StatusVenda status);

    @Query("SELECT SUM(v.valorFinal) FROM Venda v WHERE v.organizationId = :organizationId")
    BigDecimal sumValorFinalByOrganization(@Param("organizationId") Integer organizationId);

    @Query("SELECT SUM(v.valorFinal) FROM Venda v WHERE v.organizationId = :organizationId AND v.status = :status")
    BigDecimal sumValorFinalByOrganizationAndStatus(@Param("organizationId") Integer organizationId,
                                                    @Param("status") StatusVenda status);

    @Query("""
            SELECT COALESCE(SUM(v.valorFinal), 0)
            FROM Venda v
            WHERE (:organizationId IS NULL OR v.organizationId = :organizationId)
            """)
    BigDecimal sumValorFinalGlobal(@Param("organizationId") Integer organizationId);

    @Query("SELECT COALESCE(SUM(v.valorFinal), 0) FROM Venda v " +
            "WHERE v.organizationId = :organizationId " +
            "AND v.dataEfetuacao BETWEEN :inicio AND :fim")
    BigDecimal sumValorFinalByOrganizationBetweenDates(@Param("organizationId") Integer organizationId,
                                                       @Param("inicio") LocalDate inicio,
                                                       @Param("fim") LocalDate fim);

    @Query("""
            SELECT YEAR(v.dataEfetuacao) AS ano,
                   MONTH(v.dataEfetuacao) AS mes,
                   COALESCE(SUM(v.valorFinal), 0) AS total
            FROM Venda v
            WHERE (:organizationId IS NULL OR v.organizationId = :organizationId)
              AND v.dataEfetuacao BETWEEN :inicio AND :fim
            GROUP BY YEAR(v.dataEfetuacao), MONTH(v.dataEfetuacao)
            ORDER BY ano, mes
            """)
    List<Object[]> sumValorFinalByMonthGlobal(@Param("organizationId") Integer organizationId,
                                              @Param("inicio") LocalDate inicio,
                                              @Param("fim") LocalDate fim);

    @Query("""
            SELECT COUNT(v)
            FROM Venda v
            WHERE (:organizationId IS NULL OR v.organizationId = :organizationId)
            """)
    long countByOrganizationOrGlobal(@Param("organizationId") Integer organizationId);

    @Query("""
            SELECT COUNT(DISTINCT v.organizationId)
            FROM Venda v
            WHERE (:inicio IS NULL OR v.dataEfetuacao >= :inicio)
              AND (:fim IS NULL OR v.dataEfetuacao <= :fim)
            """)
    long countDistinctOrganizationsByPeriodo(@Param("inicio") LocalDate inicio,
                                             @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(v) FROM Venda v " +
            "WHERE v.organizationId = :organizationId " +
            "AND v.cliente.id = :clienteId " +
            "AND v.status = :status")
    long countByClienteAndStatus(@Param("organizationId") Integer organizationId,
                                 @Param("clienteId") Integer clienteId,
                                 @Param("status") StatusVenda status);

    @Query("""
            SELECT v FROM Venda v
            WHERE v.organizationId = :organizationId
              AND v.createdBy = :userId
              AND v.dataAgendada IS NOT NULL
              AND (:inicio IS NULL OR v.dataAgendada >= :inicio)
              AND (:fim IS NULL OR v.dataAgendada <= :fim)
            """)
    List<Venda> findAgendadasNoPeriodo(@Param("organizationId") Integer organizationId,
                                       @Param("userId") Integer userId,
                                       @Param("inicio") LocalDate inicio,
                                       @Param("fim") LocalDate fim);

    @Query("""
            SELECT v.organizationId, MIN(v.dataEfetuacao)
            FROM Venda v
            WHERE (:inicio IS NULL OR v.dataEfetuacao >= :inicio)
              AND (:fim IS NULL OR v.dataEfetuacao <= :fim)
            GROUP BY v.organizationId
            """)
    List<Object[]> findPrimeiraVendaPorOrganizacao(@Param("inicio") LocalDate inicio,
                                                   @Param("fim") LocalDate fim);

    @Query("""
            SELECT v.dataEfetuacao AS dia,
                   COUNT(DISTINCT v.organizationId) AS quantidade
            FROM Venda v
            WHERE v.dataEfetuacao IS NOT NULL
              AND v.dataEfetuacao BETWEEN :inicio AND :fim
            GROUP BY v.dataEfetuacao
            ORDER BY v.dataEfetuacao
            """)
    List<Object[]> countDistinctOrganizationsWithSalesByDate(@Param("inicio") LocalDate inicio,
                                                              @Param("fim") LocalDate fim);

    @Query("""
            SELECT DISTINCT v.organizationId
            FROM Venda v
            WHERE v.dataEfetuacao IS NOT NULL
              AND v.dataEfetuacao BETWEEN :inicio AND :fim
            """)
    List<Integer> findDistinctOrganizationIdsWithSalesBetween(@Param("inicio") LocalDate inicio,
                                                               @Param("fim") LocalDate fim);

    @Query("""
            SELECT COUNT(DISTINCT v.organizationId)
            FROM Venda v
            WHERE v.dataEfetuacao IS NOT NULL
              AND v.dataEfetuacao BETWEEN :inicio AND :fim
              AND (:excludedOrganizationId IS NULL OR v.organizationId <> :excludedOrganizationId)
            """)
    long countDistinctOrganizationsWithSalesBetween(@Param("inicio") LocalDate inicio,
                                                    @Param("fim") LocalDate fim,
                                                    @Param("excludedOrganizationId") Integer excludedOrganizationId);
}
