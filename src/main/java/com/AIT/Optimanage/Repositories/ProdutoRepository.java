package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    Page<Produto> findAllByAtivoTrue(Pageable pageable);

    Optional<Produto> findByIdAndAtivoTrue(Integer idProduto);
}