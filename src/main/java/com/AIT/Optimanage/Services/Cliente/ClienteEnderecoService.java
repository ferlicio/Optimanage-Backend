package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteEnderecoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteEnderecoService {

    private final ClienteEnderecoRepository clienteEnderecoRepository;
    private final ClienteService clienteService;

    public List<ClienteEndereco> listarEnderecos(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        return clienteEnderecoRepository.findAllByCliente_IdAndClienteOwnerUser(idCliente, loggedUser);
    }

    public ClienteEndereco listarUmEndereco(Integer idCliente, Integer idEndereco) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteService.listarUmCliente(idCliente);
        return clienteEnderecoRepository.findByIdAndCliente_IdAndClienteOwnerUser(idEndereco, cliente.getId(), loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public ClienteEndereco cadastrarEndereco(Integer idCliente, ClienteEndereco endereco) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteService.listarUmCliente(idCliente);

        endereco.setId(null);
        endereco.setCliente(cliente);
        return clienteEnderecoRepository.save(endereco);
    }

    public ClienteEndereco editarEndereco(Integer idCliente, Integer idEndereco, ClienteEndereco endereco) {
        ClienteEndereco enderecoExistente = listarUmEndereco(idCliente, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setCliente(enderecoExistente.getCliente());
        return clienteEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(Integer idCliente, Integer idEndereco) {
        ClienteEndereco endereco = listarUmEndereco(idCliente, idEndereco);
        clienteEnderecoRepository.delete(endereco);
    }
}
