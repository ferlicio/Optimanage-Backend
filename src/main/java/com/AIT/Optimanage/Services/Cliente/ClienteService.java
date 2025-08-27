package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Cacheable(value = "clientes", key = "#loggedUser.id + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(User loggedUser, ClienteSearch pesquisa) {
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuário logado
        return clienteRepository.buscarClientes(
                loggedUser.getId(),
                pesquisa.getId(),
                pesquisa.getNome(),
                pesquisa.getCpfOuCnpj(),
                pesquisa.getAtividade(),
                pesquisa.getEstado(),
                pesquisa.getTipoPessoa(),
                pesquisa.getAtivo() != null ? pesquisa.getAtivo() : true,
                pageable
        );
    }

    public Cliente listarUmCliente(User loggedUser, Integer idCliente) {
        return clienteRepository.findByIdAndOwnerUserAndAtivoTrue(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    public Cliente criarCliente(User loggedUser, ClienteRequest request) {
        Cliente cliente = fromRequest(request);
        cliente.setId(null);
        cliente.setOwnerUser(loggedUser);
        cliente.setDataCadastro(LocalDate.now());
        validarCliente(loggedUser, cliente);
        return clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    public Cliente editarCliente(User loggedUser, Integer idCliente, ClienteRequest request) {
        Cliente clienteSalvo = listarUmCliente(loggedUser, idCliente);
        Cliente cliente = fromRequest(request);
        cliente.setId(clienteSalvo.getId());
        cliente.setOwnerUser(clienteSalvo.getOwnerUser());
        cliente.setDataCadastro(clienteSalvo.getDataCadastro());
        validarCliente(loggedUser, cliente);
        return clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    public void inativarCliente(User loggedUser, Integer idCliente) {
        Cliente cliente = listarUmCliente(loggedUser, idCliente);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    public Cliente reativarCliente(User loggedUser, Integer idCliente) {
        Cliente cliente = clienteRepository.findByIdAndOwnerUser(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        cliente.setAtivo(true);
        return clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    public void removerCliente(User loggedUser, Integer idCliente) {
        Cliente cliente = clienteRepository.findByIdAndOwnerUser(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        clienteRepository.delete(cliente);
    }


    public void validarCliente(User loggedUser, Cliente cliente) {
        if (cliente.getTipoPessoa() == TipoPessoa.PF) {
            cliente.setNomeFantasia(null);
            cliente.setRazaoSocial(null);
            cliente.setCnpj(null);
            cliente.setInscricaoEstadual(null);
            cliente.setInscricaoMunicipal(null);
        } else {
            if (cliente.getCnpj() == null || cliente.getCnpj().isEmpty()) {
                throw new IllegalArgumentException("CNPJ é obrigatório");
            }
            cliente.setNome(null);
            cliente.setCpf(null);
        }

    }

    private Cliente fromRequest(ClienteRequest request) {
        Cliente cliente = new Cliente();
        Atividade atividade = new Atividade();
        atividade.setId(request.getAtividadeId());
        cliente.setAtividade(atividade);
        cliente.setTipoPessoa(request.getTipoPessoa());
        cliente.setOrigem(request.getOrigem());
        cliente.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
        cliente.setNome(request.getNome());
        cliente.setNomeFantasia(request.getNomeFantasia());
        cliente.setRazaoSocial(request.getRazaoSocial());
        cliente.setCpf(request.getCpf());
        cliente.setCnpj(request.getCnpj());
        cliente.setInscricaoEstadual(request.getInscricaoEstadual());
        cliente.setInscricaoMunicipal(request.getInscricaoMunicipal());
        cliente.setSite(request.getSite());
        cliente.setInformacoesAdicionais(request.getInformacoesAdicionais());
        return cliente;
    }
}
