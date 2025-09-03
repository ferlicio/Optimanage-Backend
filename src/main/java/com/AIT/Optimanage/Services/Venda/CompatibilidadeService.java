package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.CompatibilidadeDTO;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Venda.Compatibilidade.CompatibilidadeRepository;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompatibilidadeService {

    private final CompatibilidadeRepository compatibilidadeRepository;
    private final ContextoCompatibilidadeService contextoCompatibilidadeService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;

    public List<Compatibilidade> buscarCompatibilidades(User logedUser, String nomeContexto) {
        ContextoCompatibilidade contexto = contextoCompatibilidadeService.listarUmContextoPorNome(nomeContexto);
        return compatibilidadeRepository.findByContexto_IdAndCompativelIsTrue(contexto.getId());
    }

    public Compatibilidade adicionarCompatibilidade(User logedUser, CompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = contextoCompatibilidadeService.listarUmContexto(logedUser, request.getContextoId());

        Compatibilidade compatibilidade = Compatibilidade.builder()
                .produto(request.getProdutoId() != null ? produtoService.buscarProdutoAtivo(request.getProdutoId()) : null)
                .servico(request.getServicoId() != null ? servicoService.buscarServicoAtivo(request.getServicoId()) : null)
                .contexto(contexto)
                .compativel(request.getCompativel())
                .build();

        return compatibilidadeRepository.save(compatibilidade);
    }
}