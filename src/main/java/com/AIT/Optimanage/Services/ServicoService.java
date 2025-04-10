package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;


    public List<Servico> listarServicos(User loggedUser) {
        return servicoRepository.findAllByOwnerUser(loggedUser);
    }

    public Servico listarUmServico(User loggedUser, Integer idServico) {
        return servicoRepository.findByIdAndOwnerUser(idServico, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Servico não encontrado")
        );
    }

    public Servico cadastrarServico(User loggedUser, Servico servico) {
        servico.setId(null);
        servico.setOwnerUser(loggedUser);
        return servicoRepository.save(servico);
    }

    public Servico editarServico(User loggedUser, Integer idServico, Servico servico) {
        Servico servicoSalvo = listarUmServico(loggedUser, idServico);
        servico.setId(servicoSalvo.getId());
        servico.setOwnerUser(servicoSalvo.getOwnerUser());
        return servicoRepository.save(servico);
    }

    public void excluirServico(User loggedUser, Integer idServico) {
        Servico servico = listarUmServico(loggedUser, idServico);
        servicoRepository.delete(servico);
    }
}
