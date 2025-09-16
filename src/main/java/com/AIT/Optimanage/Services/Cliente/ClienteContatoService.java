package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
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
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return clienteContatoRepository.findAllByCliente_IdAndClienteOrganizationId(idCliente, organizationId);
    }

    public ClienteContato listarUmContato(Integer idCliente, Integer idContato) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Cliente cliente = clienteService.listarUmCliente(idCliente);
        return clienteContatoRepository.findByIdAndCliente_IdAndClienteOrganizationId(idContato, cliente.getId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public ClienteContato cadastrarContato(Integer idCliente, ClienteContato contato) {
        Cliente cliente = clienteService.listarUmCliente(idCliente);

        contato.setId(null);
        contato.setCliente(cliente);
        contato.setTenantId(cliente.getOrganizationId());
        return clienteContatoRepository.save(contato);
    }

    public ClienteContato editarContato(Integer idCliente, Integer idContato, ClienteContato contato) {
        ClienteContato contatoExistente = listarUmContato(idCliente, idContato);

        contato.setId(contatoExistente.getId());
        contato.setCliente(contatoExistente.getCliente());
        contato.setTenantId(contatoExistente.getOrganizationId());
        return clienteContatoRepository.save(contato);
    }

    public void excluirContato(Integer idCliente, Integer idContato) {
        ClienteContato contato = listarUmContato(idCliente, idContato);
        clienteContatoRepository.delete(contato);
    }
}
