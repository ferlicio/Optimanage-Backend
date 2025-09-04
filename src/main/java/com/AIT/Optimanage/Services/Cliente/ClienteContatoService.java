package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteContatoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteContatoService {

    private final ClienteContatoRepository clienteContatoRepository;
    private final ClienteService clienteService;

    public List<ClienteContato> listarContatos(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        return clienteContatoRepository.findAllByCliente_IdAndClienteOwnerUser(idCliente, loggedUser);
    }

    public ClienteContato listarUmContato(Integer idCliente, Integer idContato) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteService.listarUmCliente(idCliente);
        return clienteContatoRepository.findByIdAndCliente_IdAndClienteOwnerUser(idContato, cliente.getId(), loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public ClienteContato cadastrarContato(Integer idCliente, ClienteContato contato) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteService.listarUmCliente(idCliente);

        contato.setId(null);
        contato.setCliente(cliente);
        return clienteContatoRepository.save(contato);
    }

    public ClienteContato editarContato(Integer idCliente, Integer idContato, ClienteContato contato) {
        ClienteContato contatoExistente = listarUmContato(idCliente, idContato);

        contato.setId(contatoExistente.getId());
        contato.setCliente(contatoExistente.getCliente());
        return clienteContatoRepository.save(contatoExistente);
    }

    public void excluirContato(Integer idCliente, Integer idContato) {
        ClienteContato contato = listarUmContato(idCliente, idContato);
        clienteContatoRepository.delete(contato);
    }
}
