package com.AIT.Optimanage.Repositories.Cliente;

import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteEnderecoRepository extends JpaRepository<ClienteEndereco, Integer> {

    List<ClienteEndereco> findAllByCliente_IdAndClienteOrganizationId(Integer idCliente, Integer organizationId);

    Optional<ClienteEndereco> findByIdAndCliente_IdAndClienteOrganizationId(Integer idEndereco, Integer idCliente, Integer organizationId);
}
