package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Integer>, JpaSpecificationExecutor<Compra> {
    Optional<Compra> findByIdAndOwnerUser(Integer idCompra, User loggedUser);
}
