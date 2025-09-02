package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Venda.Compatibilidade.ContextoCompatibilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContextoCompatibilidadeService {

    private final ContextoCompatibilidadeRepository contextoRepository;

    public ContextoCompatibilidade listarContextos() {
        return contextoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade listarUmContexto(Integer idContexto) {
        return contextoRepository.findById(idContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade listarUmContextoPorNome(String nomeContexto) {
        return contextoRepository.findByNome(nomeContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade criarContexto(User logedUser, ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = listarUmContextoPorNome(request.getNome());
        if (contexto != null) {
            throw new RuntimeException("Contexto já existe!");
        } else {
            return contextoRepository.save(ContextoCompatibilidade.builder()
                    .ownerUser(logedUser)
                    .nome(request.getNome())
                    .build());
            }
    }

    public ContextoCompatibilidade editarContexto(User loggedUser, Integer idContexto, ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = listarUmContexto(idContexto);
        contexto.setNome(request.getNome());
        return contextoRepository.save(contexto);
    }

    public void excluirContexto(User loggedUser, Integer idContexto) {
        ContextoCompatibilidade contexto = listarUmContexto(idContexto);
        contextoRepository.delete(contexto);
    }
}