package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Controllers.dto.ClienteResponse;
import com.AIT.Optimanage.Mappers.ClienteMapper;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Support.PaginationUtils;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PlanoAccessGuard planoAccessGuard;
    private final PlanoService planoService;
    private final VendaRepository vendaRepository;

    @Cacheable(value = "clientes", key = "T(com.AIT.Optimanage.Support.CacheKeyResolver).userScopedKey(#pesquisa)")
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listarClientes(ClienteSearch pesquisa) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        int page = PaginationUtils.resolvePage(pesquisa.getPage());
        int pageSize = PaginationUtils.resolvePageSize(pesquisa.getPageSize(), null);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuário logado
        return clienteRepository.buscarClientes(
                organizationId,
                pesquisa.getId(),
                pesquisa.getNome(),
                pesquisa.getCpfOuCnpj(),
                pesquisa.getAtividade(),
                pesquisa.getEstado(),
                pesquisa.getTipoPessoa(),
                pesquisa.getAtivo() != null ? pesquisa.getAtivo() : true,
                pageable
        ).map(clienteMapper::toResponse);
    }

    public Cliente listarUmCliente(Integer idCliente) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return clienteRepository.findByIdAndOrganizationIdAndAtivoTrue(idCliente, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    public ClienteResponse listarUmClienteResponse(Integer idCliente) {
        return clienteMapper.toResponse(listarUmCliente(idCliente));
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse criarCliente(ClienteRequest request) {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Plano plano = planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        long clientesAtivos = clienteRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteClientes(plano, clientesAtivos, 1);
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setId(null);
        cliente.setTenantId(organizationId);
        cliente.setDataCadastro(LocalDate.now());
        cliente.setLifetimeValue(BigDecimal.ZERO);
        cliente.setChurnScore(BigDecimal.ZERO);
        cliente.setAverageTicket(BigDecimal.ZERO);
        validarCliente(cliente);
        Cliente salvo = clienteRepository.save(cliente);
        return clienteMapper.toResponse(salvo);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse editarCliente(Integer idCliente, ClienteRequest request) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Cliente clienteSalvo = clienteRepository.findByIdAndOrganizationIdAndAtivoTrue(idCliente, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setId(clienteSalvo.getId());
        cliente.setDataCadastro(clienteSalvo.getDataCadastro());
        cliente.setTenantId(clienteSalvo.getOrganizationId());
        cliente.setLifetimeValue(clienteSalvo.getLifetimeValue());
        cliente.setChurnScore(clienteSalvo.getChurnScore());
        cliente.setAverageTicket(clienteSalvo.getAverageTicket());
        validarCliente(cliente);
        Cliente atualizado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(atualizado);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void inativarCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Cliente cliente = clienteRepository.findByIdAndOrganizationIdAndAtivoTrue(idCliente, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponse reativarCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Cliente cliente = clienteRepository.findByIdAndOrganizationId(idCliente, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            Plano plano = planoService.obterPlanoUsuario(loggedUser)
                    .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
            long clientesAtivos = clienteRepository.countByOrganizationIdAndAtivoTrue(organizationId);
            validarLimiteClientes(plano, clientesAtivos, 1);
        }
        cliente.setAtivo(true);
        Cliente atualizado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(atualizado);
    }

    @CacheEvict(value = "clientes", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void removerCliente(Integer idCliente) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Cliente cliente = clienteRepository.findByIdAndOrganizationId(idCliente, organizationId)
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


    private void validarLimiteClientes(Plano plano, long clientesAtivos, int novosClientes) {
        Integer limite = plano.getMaxClientes();
        if (limite != null && limite > 0 && clientesAtivos + novosClientes > limite) {
            throw new IllegalStateException("Limite de clientes do plano atingido");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void atualizarMetricasCliente(Cliente cliente) {
        if (cliente == null || cliente.getId() == null || cliente.getOrganizationId() == null) {
            return;
        }

        BigDecimal lifetimeValue = Optional.ofNullable(
                vendaRepository.sumValorFinalByClienteAndStatus(
                        cliente.getOrganizationId(),
                        cliente.getId(),
                        StatusVenda.CONCRETIZADA))
                .orElse(BigDecimal.ZERO);

        long vendasConcretizadas = vendaRepository.countByClienteAndStatus(
                cliente.getOrganizationId(),
                cliente.getId(),
                StatusVenda.CONCRETIZADA);

        long vendasCanceladas = vendaRepository.countByClienteAndStatus(
                cliente.getOrganizationId(),
                cliente.getId(),
                StatusVenda.CANCELADA);

        long totalRelevante = vendasConcretizadas + vendasCanceladas;

        BigDecimal churnScore = BigDecimal.ZERO;
        if (totalRelevante > 0) {
            churnScore = BigDecimal.valueOf(vendasCanceladas)
                    .divide(BigDecimal.valueOf(totalRelevante), 4, RoundingMode.HALF_UP);
        }

        cliente.setLifetimeValue(lifetimeValue);
        cliente.setChurnScore(churnScore);
        BigDecimal averageTicket = BigDecimal.ZERO;
        if (vendasConcretizadas > 0) {
            averageTicket = lifetimeValue
                    .divide(BigDecimal.valueOf(vendasConcretizadas), 2, RoundingMode.HALF_UP);
        }
        cliente.setAverageTicket(averageTicket);
        clienteRepository.save(cliente);
    }

}
