package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.User.UserInfo;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.User.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final UserInfoRepository userInfoRepository;

    public List<PlanoResponse> listarPlanos() {
        return planoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlanoResponse criarPlano(PlanoRequest request) {
        Plano plano = fromRequest(request);
        plano.setId(null);
        Plano salvo = planoRepository.save(plano);
        return toResponse(salvo);
    }

    @Transactional
    public PlanoResponse atualizarPlano(Integer idPlano, PlanoRequest request) {
        Plano existente = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        Plano plano = fromRequest(request);
        plano.setId(existente.getId());
        Plano atualizado = planoRepository.save(plano);
        return toResponse(atualizado);
    }

    @Transactional
    public void removerPlano(Integer idPlano) {
        Plano plano = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        planoRepository.delete(plano);
    }

    public Optional<Plano> obterPlanoUsuario(User user) {
        return userInfoRepository.findByOwnerUser(user)
                .map(UserInfo::getPlanoAtivoId);
    }

    private Plano fromRequest(PlanoRequest request) {
        return Plano.builder()
                .nome(request.getNome())
                .valor(request.getValor())
                .duracaoDias(request.getDuracaoDias())
                .qtdAcessos(request.getQtdAcessos())
                .build();
    }

    private PlanoResponse toResponse(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .valor(plano.getValor())
                .duracaoDias(plano.getDuracaoDias())
                .qtdAcessos(plano.getQtdAcessos())
                .build();
    }
}

