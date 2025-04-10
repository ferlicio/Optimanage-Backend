package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findAllByOwnerUser(User loggedUser);

    Optional<Produto> findByIdAndOwnerUser(Integer idProduto, User loggedUser);
}