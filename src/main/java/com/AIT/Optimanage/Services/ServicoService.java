package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Mappers.ServicoMapper;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
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

    @Cacheable(value = "servicos", key = "#loggedUser.id + '-' + #pesquisa.hashCode()")
    public Page<ServicoResponse> listarServicos(User loggedUser, Search pesquisa) {
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return servicoRepository.findAllByOwnerUserAndAtivoTrue(loggedUser, pageable)
                .map(servicoMapper::toResponse);
    }

    public ServicoResponse listarUmServico(User loggedUser, Integer idServico) {
        Servico servico = buscarServicoAtivo(loggedUser, idServico);
        return servicoMapper.toResponse(servico);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "#loggedUser.id")
    public ServicoResponse cadastrarServico(User loggedUser, ServicoRequest request) {
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(null);
        Servico salvo = servicoRepository.save(servico);
        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "#loggedUser.id")
    public ServicoResponse editarServico(User loggedUser, Integer idServico, ServicoRequest request) {
        Servico servicoSalvo = buscarServicoAtivo(loggedUser, idServico);
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(servicoSalvo.getId());
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "#loggedUser.id")
    public void excluirServico(User loggedUser, Integer idServico) {
        Servico servico = buscarServicoAtivo(loggedUser, idServico);
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    public ServicoResponse restaurarServico(User loggedUser, Integer idServico) {
        Servico servico = buscarServico(loggedUser, idServico);
        servico.setAtivo(true);
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toResponse(atualizado);
    }

    public void removerServico(User loggedUser, Integer idServico) {
        Servico servico = buscarServico(loggedUser, idServico);
        servicoRepository.delete(servico);
    }

    private Servico buscarServico(User loggedUser, Integer idServico) {
        return servicoRepository.findByIdAndOwnerUser(idServico, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }

    public Servico buscarServicoAtivo(User loggedUser, Integer idServico) {
        return servicoRepository.findByIdAndOwnerUserAndAtivoTrue(idServico, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado"));
    }
}

