package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    Page<Produto> findAllByOrganizationIdAndAtivoTrue(Integer organizationId, Pageable pageable);

    Optional<Produto> findByIdAndOrganizationIdAndAtivoTrue(Integer idProduto, Integer organizationId);

    Optional<Produto> findByIdAndOrganizationId(Integer idProduto, Integer organizationId);

    List<Produto> findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(Collection<Integer> ids, Integer organizationId);

    @Modifying
    @Query("update Produto p set p.qtdEstoque = p.qtdEstoque - :quantidade where p.id = :id and p.qtdEstoque >= :quantidade")
    int reduzirEstoque(Integer id, Integer quantidade);

    @Modifying
    @Query("update Produto p set p.qtdEstoque = p.qtdEstoque + :quantidade where p.id = :id")
    int incrementarEstoque(Integer id, Integer quantidade);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);
}
