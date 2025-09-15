package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Mappers.PlanoMapper;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final OrganizationRepository organizationRepository;
    private final PlanoMapper planoMapper;

    public List<PlanoResponse> listarPlanos() {
        return planoRepository.findAll()
                .stream()
                .map(planoMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanoResponse criarPlano(PlanoRequest request) {
        Plano plano = planoMapper.toEntity(request);
        plano.setId(null);
        Plano salvo = planoRepository.save(plano);
        return planoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "planos", allEntries = true)
    public PlanoResponse atualizarPlano(Integer idPlano, PlanoRequest request) {
        Plano existente = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        Plano plano = planoMapper.toEntity(request);
        plano.setId(existente.getId());
        Plano atualizado = planoRepository.save(plano);
        return planoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "planos", allEntries = true)
    public void removerPlano(Integer idPlano) {
        Plano plano = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        planoRepository.delete(plano);
    }

    @Cacheable(value = "planos", key = "#user.id")
    public Optional<Plano> obterPlanoUsuario(User user) {
        return organizationRepository.findById(user.getTenantId())
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById);
    }
}

