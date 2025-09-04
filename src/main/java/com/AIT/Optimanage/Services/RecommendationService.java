package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> recomendarProdutos(Integer clienteId) {
        User loggedUser = CurrentUser.get();
        List<Object[]> historicoCliente = vendaRepository.findTopProdutosByCliente(clienteId, loggedUser.getId());

        Set<Integer> produtosCliente = historicoCliente.stream()
                .map(r -> (Integer) r[0])
                .collect(Collectors.toSet());

        if (produtosCliente.isEmpty()) {
            return Collections.emptyList();
        }

        List<Venda> vendas = vendaRepository.findAllWithProdutosByOwnerUser(loggedUser.getId());

        Map<Integer, Integer> contagemRecomendacoes = new HashMap<>();
        for (Venda venda : vendas) {
            Set<Integer> produtosVenda = venda.getVendaProdutos().stream()
                    .map(vp -> vp.getProduto().getId())
                    .collect(Collectors.toSet());
            if (Collections.disjoint(produtosVenda, produtosCliente)) {
                continue;
            }
            for (Integer produto : produtosVenda) {
                if (!produtosCliente.contains(produto)) {
                    contagemRecomendacoes.merge(produto, 1, Integer::sum);
                }
            }
        }

        List<Integer> recomendadosIds = contagemRecomendacoes.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<Produto> produtos = produtoRepository.findAllById(recomendadosIds);

        return produtos.stream().map(this::toResponse).toList();
    }

    private ProdutoResponse toResponse(Produto produto) {
        return ProdutoResponse.builder()
                .id(produto.getId())
                .ownerUserId(produto.getOwnerUser() != null ? produto.getOwnerUser().getId() : null)
                .fornecedorId(produto.getFornecedor() != null ? produto.getFornecedor().getId() : null)
                .sequencialUsuario(produto.getSequencialUsuario())
                .codigoReferencia(produto.getCodigoReferencia())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .custo(produto.getCusto())
                .disponivelVenda(produto.getDisponivelVenda())
                .valorVenda(produto.getValorVenda())
                .qtdEstoque(produto.getQtdEstoque())
                .terceirizado(produto.getTerceirizado())
                .ativo(produto.getAtivo())
                .build();
    }
}
