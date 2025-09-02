package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    Page<Produto> findAllByOwnerUserAndAtivoTrue(User loggedUser, Pageable pageable);

    Optional<Produto> findByIdAndOwnerUserAndAtivoTrue(Integer idProduto, User loggedUser);

    Optional<Produto> findByIdAndOwnerUser(Integer idProduto, User loggedUser);

    @Modifying
    @Query("update Produto p set p.qtdEstoque = p.qtdEstoque - :quantidade where p.id = :id and p.qtdEstoque >= :quantidade")
    int reduzirEstoque(Integer id, Integer quantidade);

    @Modifying
    @Query("update Produto p set p.qtdEstoque = p.qtdEstoque + :quantidade where p.id = :id")
    int incrementarEstoque(Integer id, Integer quantidade);
}
