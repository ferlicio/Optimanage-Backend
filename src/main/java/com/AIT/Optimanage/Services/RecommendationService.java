package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.Venda.CompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final PlanoService planoService;
    private final CompatibilidadeService compatibilidadeService;

    private static final int MAX_SUGESTOES = 10;
    private static final int CANDIDATOS_MULTIPLICADOR = 3;
    private static final double COMPATIBILIDADE_BONUS = 1.5;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> recomendarProdutos(Integer clienteId) {
        return recomendarProdutos(clienteId, null, true);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> recomendarProdutos(Integer clienteId, String contexto) {
        return recomendarProdutos(clienteId, contexto, true);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> recomendarProdutos(Integer clienteId, String contexto, Boolean apenasEstoquePositivo) {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Plano plano = planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        if (!Boolean.TRUE.equals(plano.getRecomendacoesHabilitadas())) {
            throw new AccessDeniedException("Recomendações não estão habilitadas no plano atual");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        List<Object[]> historicoCliente = vendaRepository.findTopProdutosByCliente(clienteId, organizationId);

        Set<Integer> produtosCliente = historicoCliente.stream()
                .map(r -> (Integer) r[0])
                .collect(Collectors.toSet());

        if (produtosCliente.isEmpty()) {
            return Collections.emptyList();
        }

        List<Venda> vendas = vendaRepository.findAllWithProdutosByOrganization(organizationId);

        Set<Integer> produtosCompativeis = buscarProdutosCompativeis(loggedUser, contexto);
        boolean filtrarEstoquePositivo = apenasEstoquePositivo == null || apenasEstoquePositivo;

        Map<Integer, Double> pontuacaoRecomendacoes = new HashMap<>();
        LocalDate referencia = calcularDataReferencia(vendas);

        for (Venda venda : vendas) {
            if (venda.getVendaProdutos() == null || venda.getVendaProdutos().isEmpty()) {
                continue;
            }
            Set<Integer> produtosVenda = venda.getVendaProdutos().stream()
                    .map(VendaProduto::getProduto)
                    .filter(Objects::nonNull)
                    .map(Produto::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (Collections.disjoint(produtosVenda, produtosCliente)) {
                continue;
            }

            double fatorRecencia = calcularFatorRecencia(venda, referencia);
            long itensClienteNaVenda = venda.getVendaProdutos().stream()
                    .map(VendaProduto::getProduto)
                    .filter(Objects::nonNull)
                    .map(Produto::getId)
                    .filter(produtosCliente::contains)
                    .count();

            for (VendaProduto vendaProduto : venda.getVendaProdutos()) {
                Produto produto = vendaProduto.getProduto();
                if (produto == null || produto.getId() == null) {
                    continue;
                }
                Integer produtoId = produto.getId();
                if (produtosCliente.contains(produtoId)) {
                    continue;
                }

                double quantidade = Optional.ofNullable(vendaProduto.getQuantidade()).orElse(0);
                double base = 1.0 + quantidade + itensClienteNaVenda;
                double score = base * fatorRecencia;

                if (produtosCompativeis.contains(produtoId)) {
                    score += COMPATIBILIDADE_BONUS;
                }

                pontuacaoRecomendacoes.merge(produtoId, score, Double::sum);
            }
        }

        if (pontuacaoRecomendacoes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ordenadosPorPontuacao = pontuacaoRecomendacoes.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        if (ordenadosPorPontuacao.isEmpty()) {
            return Collections.emptyList();
        }

        int limiteBusca = Math.min(ordenadosPorPontuacao.size(), MAX_SUGESTOES * CANDIDATOS_MULTIPLICADOR);
        List<Integer> idsParaBusca = ordenadosPorPontuacao.stream()
                .limit(limiteBusca)
                .toList();

        if (idsParaBusca.isEmpty()) {
            return Collections.emptyList();
        }

        List<Produto> produtos = produtoRepository
                .findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(idsParaBusca, organizationId);

        if (produtos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Produto> produtosPorId = produtos.stream()
                .filter(produto -> produto.getId() != null)
                .collect(Collectors.toMap(Produto::getId, Function.identity(), (a, b) -> a));

        return ordenadosPorPontuacao.stream()
                .map(produtosPorId::get)
                .filter(Objects::nonNull)
                .filter(produto -> !filtrarEstoquePositivo || estoqueDisponivel(produto))
                .limit(MAX_SUGESTOES)
                .map(this::toResponse)
                .toList();
    }

    private boolean estoqueDisponivel(Produto produto) {
        return Optional.ofNullable(produto.getQtdEstoque()).orElse(0) > 0;
    }

    private Set<Integer> buscarProdutosCompativeis(User loggedUser, String contexto) {
        if (contexto == null || contexto.isBlank()) {
            return Collections.emptySet();
        }
        try {
            return compatibilidadeService.buscarCompatibilidades(loggedUser, contexto).stream()
                    .map(compatibilidade -> Optional.ofNullable(compatibilidade.getProduto())
                            .map(Produto::getId)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (RuntimeException ignored) {
            return Collections.emptySet();
        }
    }

    private LocalDate calcularDataReferencia(List<Venda> vendas) {
        return vendas.stream()
                .map(this::obterDataVenda)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
    }

    private double calcularFatorRecencia(Venda venda, LocalDate referencia) {
        LocalDate dataVenda = Optional.ofNullable(obterDataVenda(venda)).orElse(referencia);
        long dias = Math.max(0, ChronoUnit.DAYS.between(dataVenda, referencia));
        return 1.0 / (1 + dias);
    }

    private LocalDate obterDataVenda(Venda venda) {
        if (venda.getDataEfetuacao() != null) {
            return venda.getDataEfetuacao();
        }
        LocalDateTime createdAt = venda.getCreatedAt();
        return createdAt != null ? createdAt.toLocalDate() : null;
    }

    private ProdutoResponse toResponse(Produto produto) {
        return ProdutoResponse.builder()
                .id(produto.getId())
                .organizationId(produto.getOrganizationId())
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
