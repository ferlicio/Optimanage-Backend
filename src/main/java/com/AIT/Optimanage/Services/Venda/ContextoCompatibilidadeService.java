package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Venda.Compatibilidade.ContextoCompatibilidadeRepository;
import com.AIT.Optimanage.Security.CurrentUser;
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

        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return contextoRepository.findAllByOrganizationId(organizationId, pageable);
    }

    public ContextoCompatibilidade listarUmContexto(User loggedUser, Integer idContexto) {
        ContextoCompatibilidade contexto = contextoRepository.findById(idContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null || !organizationId.equals(contexto.getOrganizationId())) {
            throw new RuntimeException("Contexto não encontrado!");
        }
        return contexto;
    }

    public ContextoCompatibilidade listarUmContextoPorNome(User loggedUser, String nomeContexto) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return contextoRepository.findByOrganizationIdAndNome(organizationId, nomeContexto)
                .orElseThrow(() -> new RuntimeException("Contexto não encontrado!"));
    }

    public ContextoCompatibilidade criarContexto(User logedUser, ContextoCompatibilidadeDTO request) {
        Integer organizationId = logedUser != null ? logedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        contextoRepository.findByOrganizationIdAndNome(organizationId, request.getNome())
                .ifPresent(existing -> {
                    throw new RuntimeException("Contexto já existe!");
                });

        ContextoCompatibilidade novoContexto = ContextoCompatibilidade.builder()
                .nome(request.getNome())
                .build();
        novoContexto.setTenantId(organizationId);

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
