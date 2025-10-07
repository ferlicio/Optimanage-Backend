package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Controllers.dto.FornecedorResponse;
import com.AIT.Optimanage.Mappers.FornecedorMapper;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.Search.FornecedorSearch;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import com.AIT.Optimanage.Services.PlanoService;
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
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;
    private final PlanoAccessGuard planoAccessGuard;
    private final PlanoService planoService;

    @Cacheable(value = "fornecedores", key = "T(com.AIT.Optimanage.Support.CacheKeyResolver).userScopedKey(#pesquisa)")
    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listarFornecedores(FornecedorSearch pesquisa) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuário logado
        return fornecedorRepository.buscarFornecedores(
                organizationId,
                pesquisa.getId(),
                pesquisa.getNome(),
                pesquisa.getCpfOuCnpj(),
                pesquisa.getAtividade(),
                pesquisa.getEstado(),
                pesquisa.getTipoPessoa(),
                pesquisa.getAtivo() != null ? pesquisa.getAtivo() : true,
                pageable
        ).map(fornecedorMapper::toResponse);
    }

    public Fornecedor listarUmFornecedor(Integer idFornecedor) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return fornecedorRepository.findByIdAndOrganizationIdAndAtivoTrue(idFornecedor, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
    }

    public FornecedorResponse listarUmFornecedorResponse(Integer idFornecedor) {
        return fornecedorMapper.toResponse(listarUmFornecedor(idFornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse criarFornecedor(FornecedorRequest request) {
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
        long fornecedoresAtivos = fornecedorRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteFornecedores(plano, fornecedoresAtivos, 1);
        Fornecedor fornecedor = fornecedorMapper.toEntity(request);
        fornecedor.setTenantId(organizationId);
        fornecedor.setDataCadastro(LocalDate.now());
        fornecedor.setLeadTimeMedioDias(BigDecimal.ZERO);
        fornecedor.setTaxaEntregaNoPrazo(BigDecimal.ZERO);
        fornecedor.setCustoMedioPedido(BigDecimal.ZERO);
        validarFornecedor(fornecedor);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse editarFornecedor(Integer idFornecedor, FornecedorRequest request) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Fornecedor fornecedorSalvo = fornecedorRepository.findByIdAndOrganizationIdAndAtivoTrue(idFornecedor, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        Fornecedor fornecedor = fornecedorMapper.toEntity(request);
        fornecedor.setId(fornecedorSalvo.getId());
        fornecedor.setDataCadastro(fornecedorSalvo.getDataCadastro());
        fornecedor.setTenantId(fornecedorSalvo.getOrganizationId());
        fornecedor.setLeadTimeMedioDias(fornecedorSalvo.getLeadTimeMedioDias());
        fornecedor.setTaxaEntregaNoPrazo(fornecedorSalvo.getTaxaEntregaNoPrazo());
        fornecedor.setCustoMedioPedido(fornecedorSalvo.getCustoMedioPedido());
        validarFornecedor(fornecedor);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void inativarFornecedor(Integer idFornecedor) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOrganizationIdAndAtivoTrue(idFornecedor, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse reativarFornecedor(Integer idFornecedor) {
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
        long fornecedoresAtivos = fornecedorRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteFornecedores(plano, fornecedoresAtivos, 1);
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOrganizationIdAndAtivoFalse(idFornecedor, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedor.setAtivo(true);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void removerFornecedor(Integer idFornecedor) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOrganizationId(idFornecedor, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedorRepository.delete(fornecedor);
    }

    public void validarFornecedor(Fornecedor fornecedor) {
        if (fornecedor.getTipoPessoa() == TipoPessoa.PF) {
            fornecedor.setNomeFantasia(null);
            fornecedor.setRazaoSocial(null);
            fornecedor.setCnpj(null);
            fornecedor.setInscricaoEstadual(null);
            fornecedor.setInscricaoMunicipal(null);
        } else {
            if (fornecedor.getCnpj() == null || fornecedor.getCnpj().isEmpty()) {
                throw new IllegalArgumentException("CNPJ é obrigatório");
            }
            fornecedor.setNome(null);
            fornecedor.setCpf(null);
        }

    }

    private void validarLimiteFornecedores(Plano plano, long fornecedoresAtivos, int novosFornecedores) {
        Integer limite = plano.getMaxFornecedores();
        if (limite != null && limite > 0 && fornecedoresAtivos + novosFornecedores > limite) {
            throw new IllegalStateException("Limite de fornecedores do plano atingido");
        }
    }
}
