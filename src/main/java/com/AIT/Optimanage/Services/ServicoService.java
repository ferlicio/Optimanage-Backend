package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Mappers.ServicoMapper;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Repositories.ServicoRepository;
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

    @Cacheable(value = "servicos", key = "T(com.AIT.Optimanage.Support.CacheKeyUtils).generateKey(#pesquisa)")
    public Page<ServicoResponse> listarServicos(Search pesquisa) {
        User loggedUser = CurrentUser.get();
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return servicoRepository.findAllByOwnerUserAndAtivoTrue(loggedUser, pageable)
                .map(servicoMapper::toResponse);
    }

    public ServicoResponse listarUmServico(Integer idServico) {
        Servico servico = buscarServicoAtivo(idServico);
        return servicoMapper.toResponse(servico);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "T(com.AIT.Optimanage.Support.CacheKeyUtils).generateKey()")
    public ServicoResponse cadastrarServico(ServicoRequest request) {
        User loggedUser = CurrentUser.get();
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(null);
        Servico salvo = servicoRepository.save(servico);
        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "T(com.AIT.Optimanage.Support.CacheKeyUtils).generateKey()")
    public ServicoResponse editarServico(Integer idServico, ServicoRequest request) {
        User loggedUser = CurrentUser.get();
        Servico servicoSalvo = buscarServicoAtivo(idServico);
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(servicoSalvo.getId());
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "T(com.AIT.Optimanage.Support.CacheKeyUtils).generateKey()")
    public void excluirServico(Integer idServico) {
        User loggedUser = CurrentUser.get();
        Servico servico = buscarServicoAtivo(idServico);
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    public ServicoResponse restaurarServico(Integer idServico) {
        User loggedUser = CurrentUser.get();
        Servico servico = buscarServico(idServico);
        servico.setAtivo(true);
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    public void removerServico(Integer idServico) {
        User loggedUser = CurrentUser.get();
        Servico servico = buscarServico(idServico);
        servicoRepository.delete(servico);
    }

    private Servico buscarServico(Integer idServico) {
        User loggedUser = CurrentUser.get();
        return servicoRepository.findByIdAndOwnerUser(idServico, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }

    public Servico buscarServicoAtivo(Integer idServico) {
        User loggedUser = CurrentUser.get();
        return servicoRepository.findByIdAndOwnerUserAndAtivoTrue(idServico, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }
}

