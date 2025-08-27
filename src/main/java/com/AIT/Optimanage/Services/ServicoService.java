package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Mappers.ServicoMapper;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @Cacheable(value = "servicos", key = "#loggedUser.id")
    public List<ServicoResponse> listarServicos(User loggedUser) {
        return servicoRepository.findAllByOwnerUserAndAtivoTrue(loggedUser)
                .stream()
                .map(servicoMapper::toResponse)
                .toList();
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
        servico.setOwnerUser(loggedUser);
        Servico salvo = servicoRepository.save(servico);
        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "servicos", key = "#loggedUser.id")
    public ServicoResponse editarServico(User loggedUser, Integer idServico, ServicoRequest request) {
        Servico servicoSalvo = buscarServicoAtivo(loggedUser, idServico);
        Servico servico = servicoMapper.toEntity(request);
        servico.setId(servicoSalvo.getId());
        servico.setOwnerUser(servicoSalvo.getOwnerUser());
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

