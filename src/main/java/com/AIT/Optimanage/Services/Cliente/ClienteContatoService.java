package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Repositories.Cliente.ClienteContatoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteContatoService {

    private final ClienteContatoRepository clienteContatoRepository;
    private final ClienteService clienteService;
    private final PlanoAccessGuard planoAccessGuard;

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

        planoAccessGuard.garantirPermissaoDeEscrita(cliente.getOrganizationId());

        contato.setId(null);
        contato.setCliente(cliente);
        contato.setTenantId(cliente.getOrganizationId());
        return clienteContatoRepository.save(contato);
    }

    public ClienteContato editarContato(Integer idCliente, Integer idContato, ClienteContato contato) {
        ClienteContato contatoExistente = listarUmContato(idCliente, idContato);

        planoAccessGuard.garantirPermissaoDeEscrita(contatoExistente.getOrganizationId());

        contato.setId(contatoExistente.getId());
        contato.setCliente(contatoExistente.getCliente());
        contato.setTenantId(contatoExistente.getOrganizationId());
        return clienteContatoRepository.save(contato);
    }

    public void excluirContato(Integer idCliente, Integer idContato) {
        ClienteContato contato = listarUmContato(idCliente, idContato);
        planoAccessGuard.garantirPermissaoDeEscrita(contato.getOrganizationId());
        clienteContatoRepository.delete(contato);
    }
}
