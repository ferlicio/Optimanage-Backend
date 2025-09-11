package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Controllers.dto.ClienteResponse;
import com.AIT.Optimanage.Mappers.ClienteMapper;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Security.CurrentUser;
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
    private final ClienteMapper clienteMapper;

    @Cacheable(value = "clientes", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId() + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listarClientes(ClienteSearch pesquisa) {
        User loggedUser = CurrentUser.get();
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
                        pageable)
                .map(clienteMapper::toResponse);
    }

    public Cliente buscarCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        return clienteRepository.findByIdAndOwnerUserAndAtivoTrue(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    public ClienteResponse listarUmCliente(Integer idCliente) {
        return clienteMapper.toResponse(buscarCliente(idCliente));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse criarCliente(ClienteRequest request) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setId(null);
        cliente.setDataCadastro(LocalDate.now());
        validarCliente(cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse editarCliente(Integer idCliente, ClienteRequest request) {
        User loggedUser = CurrentUser.get();
        Cliente clienteSalvo = buscarCliente(idCliente);
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setId(clienteSalvo.getId());
        cliente.setDataCadastro(clienteSalvo.getDataCadastro());
        validarCliente(cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void inativarCliente(Integer idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse reativarCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteRepository.findByIdAndOwnerUser(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        cliente.setAtivo(true);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void removerCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        Cliente cliente = clienteRepository.findByIdAndOwnerUser(idCliente, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        clienteRepository.delete(cliente);
    }


    public void validarCliente(Cliente cliente) {
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

    
}
