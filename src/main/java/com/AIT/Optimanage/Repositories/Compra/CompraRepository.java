package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Integer>, JpaSpecificationExecutor<Compra> {
    Optional<Compra> findByIdAndOrganizationId(Integer idCompra, Integer organizationId);
}
