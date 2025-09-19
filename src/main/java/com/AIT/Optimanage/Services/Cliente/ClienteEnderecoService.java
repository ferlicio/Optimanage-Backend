package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
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
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return clienteEnderecoRepository.findAllByCliente_IdAndClienteOrganizationId(idCliente, organizationId);
    }

    public ClienteEndereco listarUmEndereco(Integer idCliente, Integer idEndereco) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Cliente cliente = clienteService.listarUmCliente(idCliente);
        return clienteEnderecoRepository.findByIdAndCliente_IdAndClienteOrganizationId(idEndereco, cliente.getId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public ClienteEndereco cadastrarEndereco(Integer idCliente, ClienteEndereco endereco) {
        Cliente cliente = clienteService.listarUmCliente(idCliente);

        endereco.setId(null);
        endereco.setCliente(cliente);
        endereco.setTenantId(cliente.getOrganizationId());
        return clienteEnderecoRepository.save(endereco);
    }

    public ClienteEndereco editarEndereco(Integer idCliente, Integer idEndereco, ClienteEndereco endereco) {
        ClienteEndereco enderecoExistente = listarUmEndereco(idCliente, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setCliente(enderecoExistente.getCliente());
        endereco.setTenantId(enderecoExistente.getOrganizationId());
        return clienteEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(Integer idCliente, Integer idEndereco) {
        ClienteEndereco endereco = listarUmEndereco(idCliente, idEndereco);
        clienteEnderecoRepository.delete(endereco);
    }
}
