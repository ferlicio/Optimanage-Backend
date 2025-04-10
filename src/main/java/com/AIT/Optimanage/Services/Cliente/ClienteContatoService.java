package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteContatoRepository;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteContatoService {

    private final ClienteContatoRepository clienteContatoRepository;
    private final ClienteService clienteService;

    public List<ClienteContato> listarContatos(User loggedUser, Integer idCliente) {
        return clienteContatoRepository.findAllByClienteIdAndClienteOwnerUser(idCliente, loggedUser);
    }

    public ClienteContato listarUmContato(User loggedUser, Integer idCliente, Integer idContato) {
        Cliente cliente = clienteService.listarUmCliente(loggedUser, idCliente);
        return clienteContatoRepository.findByIdAndClienteIdAndClienteOwnerUser(idContato, idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public ClienteContato cadastrarContato(User loggedUser, Integer idCliente, ClienteContato contato) {
        Cliente cliente = clienteService.listarUmCliente(loggedUser, idCliente);

        contato.setId(null);
        contato.setCliente(cliente);
        return clienteContatoRepository.save(contato);
    }

    public ClienteContato editarContato(User loggedUser, Integer idCliente, Integer idContato, ClienteContato contato) {
        ClienteContato contatoExistente = listarUmContato(loggedUser, idCliente, idContato);

        contato.setId(contatoExistente.getId());
        contato.setCliente(contatoExistente.getCliente());
        return clienteContatoRepository.save(contatoExistente);
    }

    public void excluirContato(User loggedUser, Integer idCliente, Integer idContato) {
        ClienteContato contato = listarUmContato(loggedUser, idCliente, idContato);
        clienteContatoRepository.delete(contato);
    }
}
