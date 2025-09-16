package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Venda.Compatibilidade.ContextoCompatibilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContextoCompatibilidadeService {

    private final ContextoCompatibilidadeRepository contextoRepository;

    public Page<ContextoCompatibilidade> listarContextos(User loggedUser, Integer page, Integer pageSize, String sort, Sort.Direction order) {
        int pageNumber = Optional.ofNullable(page).orElse(0);
        int size = Optional.ofNullable(pageSize).orElse(10);
        Sort.Direction direction = Optional.ofNullable(order).orElse(Sort.Direction.ASC);
        String sortBy = Optional.ofNullable(sort).filter(s -> !s.isBlank()).orElse("id");

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(direction, sortBy));

        return contextoRepository.findAllByOwnerUser(loggedUser, pageable);
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
        contextoRepository.findByOwnerUserAndNome(logedUser, request.getNome())
                .ifPresent(existing -> {
                    throw new RuntimeException("Contexto já existe!");
                });

        ContextoCompatibilidade novoContexto = ContextoCompatibilidade.builder()
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
