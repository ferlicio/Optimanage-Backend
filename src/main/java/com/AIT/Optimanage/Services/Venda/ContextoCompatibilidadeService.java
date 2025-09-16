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

    public ContextoCompatibilidade listarContextos(User loggedUser) {
        return contextoRepository.findByOwnerUser(loggedUser)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade listarUmContexto(User loggedUser, Integer idContexto) {
        return contextoRepository.findById(idContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade listarUmContextoPorNome(User loggedUser, String nomeContexto) {
        return contextoRepository.findByOwnerUserAndNome(loggedUser, nomeContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade criarContexto(User logedUser, ContextoCompatibilidadeDTO request) {
        if (contextoRepository.findByOwnerUserAndNome(logedUser, request.getNome()).isPresent()) {
            throw new RuntimeException("Contexto já existe!");
        }

        ContextoCompatibilidade novoContexto = ContextoCompatibilidade.builder()
                .ownerUser(logedUser)
                .nome(request.getNome())
                .build();

        return contextoRepository.save(novoContexto);
    }

    public ContextoCompatibilidade editarContexto(User loggedUser, Integer idContexto, ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = listarUmContexto(loggedUser, idContexto);
        contexto.setNome(request.getNome());
        return contextoRepository.save(contexto);
    }

    public void excluirContexto(User loggedUser, Integer idContexto) {
        ContextoCompatibilidade contexto = listarUmContexto(loggedUser, idContexto);
        contextoRepository.delete(contexto);
    }
}