package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteEnderecoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteEnderecoService {

    private final ClienteEnderecoRepository clienteEnderecoRepository;
    private final ClienteService clienteService;

    public List<ClienteEndereco> listarEnderecos(User loggedUser, Integer idCliente) {
        return clienteEnderecoRepository.findAllByCliente_Id(idCliente);
    }

    public ClienteEndereco listarUmEndereco(User loggedUser, Integer idCliente, Integer idEndereco) {
        Cliente cliente = clienteService.listarUmCliente(loggedUser, idCliente);
        return clienteEnderecoRepository.findByIdAndCliente_Id(idEndereco, cliente.getId())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public ClienteEndereco cadastrarEndereco(User loggedUser, Integer idCliente, ClienteEndereco endereco) {
        Cliente cliente = clienteService.listarUmCliente(loggedUser, idCliente);

        endereco.setId(null);
        endereco.setCliente(cliente);
        return clienteEnderecoRepository.save(endereco);
    }

    public ClienteEndereco editarEndereco(User loggedUser, Integer idCliente, Integer idEndereco, ClienteEndereco endereco) {
        ClienteEndereco enderecoExistente = listarUmEndereco(loggedUser, idCliente, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setCliente(enderecoExistente.getCliente());
        return clienteEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(User loggedUser, Integer idCliente, Integer idEndereco) {
        ClienteEndereco endereco = listarUmEndereco(loggedUser, idCliente, idEndereco);
        clienteEnderecoRepository.delete(endereco);
    }
}
