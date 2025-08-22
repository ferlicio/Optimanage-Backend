package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
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

    public Servico cadastrarServico(User loggedUser, ServicoRequest request) {
        Servico servico = fromRequest(request);
        servico.setId(null);
        servico.setOwnerUser(loggedUser);
        return servicoRepository.save(servico);
    }

    public Servico editarServico(User loggedUser, Integer idServico, ServicoRequest request) {
        Servico servicoSalvo = listarUmServico(loggedUser, idServico);
        Servico servico = fromRequest(request);
        servico.setId(servicoSalvo.getId());
        servico.setOwnerUser(servicoSalvo.getOwnerUser());
        return servicoRepository.save(servico);
    }

    public void excluirServico(User loggedUser, Integer idServico) {
        Servico servico = listarUmServico(loggedUser, idServico);
        servicoRepository.delete(servico);
    }

    private Servico fromRequest(ServicoRequest request) {
        Servico servico = new Servico();
        if (request.getFornecedorId() != null) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(request.getFornecedorId());
            servico.setFornecedor(fornecedor);
        }
        servico.setSequencialUsuario(request.getSequencialUsuario());
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setCusto(request.getCusto());
        servico.setDisponivelVenda(request.getDisponivelVenda());
        servico.setValorVenda(request.getValorVenda());
        servico.setTempoExecucao(request.getTempoExecucao());
        servico.setTerceirizado(request.getTerceirizado());
        return servico;
    }
}
