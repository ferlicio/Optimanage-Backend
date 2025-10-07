package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Mappers.ServicoMapper;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Repositories.ServicoRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;
    private final PlanoAccessGuard planoAccessGuard;
    private final PlanoService planoService;

    @Cacheable(value = "servicos", key = "T(com.AIT.Optimanage.Support.CacheKeyResolver).userScopedKey(#pesquisa)")
    public Page<ServicoResponse> listarServicos(Search pesquisa) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return servicoRepository.findAllByOrganizationIdAndAtivoTrue(organizationId, pageable)
                .map(servicoMapper::toResponse);
    }

    public ServicoResponse listarUmServico(Integer idServico) {
        Servico servico = buscarServicoAtivo(idServico);
        return servicoMapper.toResponse(servico);
    }

    @Transactional
    @CacheEvict(value = "servicos", allEntries = true)
    public ServicoResponse cadastrarServico(ServicoRequest request) {
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
        long servicosAtivos = servicoRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteServicos(plano, servicosAtivos, 1);
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(null);
        servico.setTenantId(organizationId);
        Servico salvo = servicoRepository.save(servico);
        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "servicos", allEntries = true)
    public ServicoResponse editarServico(Integer idServico, ServicoRequest request) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Servico servicoSalvo = servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(servicoSalvo.getId());
        servico.setTenantId(servicoSalvo.getOrganizationId());
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "servicos", allEntries = true)
    public void excluirServico(Integer idServico) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Servico servico = servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    public ServicoResponse restaurarServico(Integer idServico) {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Servico servico = servicoRepository.findByIdAndOrganizationId(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
        if (!Boolean.TRUE.equals(servico.getAtivo())) {
            Plano plano = planoService.obterPlanoUsuario(loggedUser)
                    .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
            long servicosAtivos = servicoRepository.countByOrganizationIdAndAtivoTrue(organizationId);
            validarLimiteServicos(plano, servicosAtivos, 1);
        }
        servico.setAtivo(true);
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    public void removerServico(Integer idServico) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        Servico servico = servicoRepository.findByIdAndOrganizationId(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
        servicoRepository.delete(servico);
    }

    private Servico buscarServico(Integer idServico) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return servicoRepository.findByIdAndOrganizationId(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }

    public Servico buscarServicoAtivo(Integer idServico) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(idServico, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }

    private void validarLimiteServicos(Plano plano, long servicosAtivos, int novosServicos) {
        Integer limite = plano.getMaxServicos();
        if (limite != null && limite > 0 && servicosAtivos + novosServicos > limite) {
            throw new IllegalStateException("Limite de serviços do plano atingido");
        }
    }
}

