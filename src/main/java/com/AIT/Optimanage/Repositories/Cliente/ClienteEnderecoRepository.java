package com.AIT.Optimanage.Repositories.Cliente;

import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteEnderecoRepository extends JpaRepository<ClienteEndereco, Integer> {

    List<ClienteEndereco> findAllByCliente_IdAndClienteOwnerUser(Integer idCliente, User loggedUser);

    Optional<ClienteEndereco> findByIdAndCliente_IdAndClienteOwnerUser(Integer idEndereco, Integer idCliente, User loggedUser);
}
