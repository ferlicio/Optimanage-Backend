package com.AIT.Optimanage.Repositories.Cliente;

import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteContatoRepository extends JpaRepository<ClienteContato, Integer> {

    List<ClienteContato> findAllByCliente_IdAndClienteOrganizationId(Integer idCliente, Integer organizationId);

    Optional<ClienteContato> findByIdAndCliente_IdAndClienteOrganizationId(Integer idContato, Integer idCliente, Integer organizationId);

    List<ClienteContato> findAllByClienteOrganizationIdAndCreatedBy(Integer organizationId, Integer userId);
}
