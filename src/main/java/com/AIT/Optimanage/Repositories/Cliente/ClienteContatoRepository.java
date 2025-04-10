package com.AIT.Optimanage.Repositories.Cliente;

import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteContatoRepository extends JpaRepository<ClienteContato, Integer> {
    
    List<ClienteContato> findAllByClienteIdAndClienteOwnerUser(Integer idCliente, User loggedUser);

    Optional<ClienteContato> findByIdAndClienteIdAndClienteOwnerUser(Integer idContato, Integer idCliente, User loggedUser);
}
