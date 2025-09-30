package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Controllers.dto.RecommendationSuggestionResponse;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Config.RecommendationProperties;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.Venda.CompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ServicoRepository servicoRepository;
    private final PlanoService planoService;
    private final CompatibilidadeService compatibilidadeService;
    private final InventoryMonitoringService inventoryMonitoringService;
    private final ClienteRepository clienteRepository;
    private final RecommendationProperties recommendationProperties;

    private static final int MAX_SUGESTOES = 10;
    private static final int CANDIDATOS_MULTIPLICADOR = 3;
    private static final double COMPATIBILIDADE_BONUS = 1.5;

    @Transactional(readOnly = true)
    public List<RecommendationSuggestionResponse> recomendarProdutos(Integer clienteId) {
        return recomendarProdutos(clienteId, null, true, null);
    }

    @Transactional(readOnly = true)
    public List<RecommendationSuggestionResponse> recomendarProdutos(Integer clienteId, String contexto) {
        return recomendarProdutos(clienteId, contexto, true, null);
    }

    @Transactional(readOnly = true)
    public List<RecommendationSuggestionResponse> recomendarProdutos(Integer clienteId,
                                                                   String contexto,
                                                                   Boolean apenasEstoquePositivo) {
        return recomendarProdutos(clienteId, contexto, apenasEstoquePositivo, null);
    }

    @Transactional(readOnly = true)
    public List<RecommendationSuggestionResponse> recomendarProdutos(Integer clienteId,
                                                                   String contexto,
                                                                   Boolean apenasEstoquePositivo,
                                                                   Boolean apenasBundles) {
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
        Set<Integer> produtosEmRisco = identificarProdutosEmRisco(organizationId);
        Cliente cliente = buscarCliente(clienteId, organizationId);
        List<Object[]> historicoCliente = buscarHistoricoBase(clienteId, organizationId, cliente != null);

        Set<Integer> produtosCliente = historicoCliente.stream()
                .map(r -> (Integer) r[0])
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        LocalDate cutoff = calcularDataCorte();
        List<Venda> vendas = vendaRepository.findRecentWithItensByOrganization(organizationId, cutoff);

        Set<Integer> produtosCompativeis = buscarProdutosCompativeis(loggedUser, contexto);
        boolean filtrarEstoquePositivo = apenasEstoquePositivo == null || apenasEstoquePositivo;
        boolean apenasBundlesFlag = Boolean.TRUE.equals(apenasBundles);

        Map<Integer, ProdutoPontuacao> pontuacaoProdutos = new HashMap<>();
        Map<BundleKey, BundlePontuacao> pontuacaoBundles = new HashMap<>();
        LocalDate referencia = calcularDataReferencia(vendas);

        for (Venda venda : vendas) {
            List<VendaProduto> itensProduto = Optional.ofNullable(venda.getVendaProdutos())
                    .orElse(Collections.emptyList());
            List<VendaServico> itensServico = Optional.ofNullable(venda.getVendaServicos())
                    .orElse(Collections.emptyList());

            if (itensProduto.isEmpty() && itensServico.isEmpty()) {
                continue;
            }

            List<VendaProduto> produtosElegiveis = itensProduto.stream()
                    .filter(Objects::nonNull)
                    .filter(vp -> vp.getProduto() != null && vp.getProduto().getId() != null)
                    .toList();

            if (produtosElegiveis.isEmpty()) {
                continue;
            }

            Set<Integer> produtosVenda = produtosElegiveis.stream()
                    .map(VendaProduto::getProduto)
                    .map(Produto::getId)
                    .collect(Collectors.toSet());
            if (!produtosCliente.isEmpty() && Collections.disjoint(produtosVenda, produtosCliente)) {
                continue;
            }

            List<VendaServico> servicosElegiveis = itensServico.stream()
                    .filter(Objects::nonNull)
                    .filter(vs -> servicoDisponivel(vs.getServico()))
                    .toList();

            double fatorRecencia = calcularFatorRecencia(venda, referencia);
            long itensClienteNaVenda = produtosCliente.isEmpty() ? 0 : produtosElegiveis.stream()
                    .map(VendaProduto::getProduto)
                    .map(Produto::getId)
                    .filter(produtosCliente::contains)
                    .count();

            Map<Integer, Double> scoresProdutosVenda = new HashMap<>();
            Map<Integer, Double> scoresServicosVenda = new HashMap<>();

            for (VendaProduto vendaProduto : produtosElegiveis) {
                Produto produto = vendaProduto.getProduto();
                Integer produtoId = produto.getId();
                if (!produtosCliente.isEmpty() && produtosCliente.contains(produtoId)) {
                    continue;
                }

                double quantidade = Optional.ofNullable(vendaProduto.getQuantidade()).orElse(0);
                double base = 1.0 + quantidade + itensClienteNaVenda;
                double score = base * fatorRecencia;

                if (produtosCompativeis.contains(produtoId)) {
                    score += COMPATIBILIDADE_BONUS;
                }

                score = ajustarScoreCliente(score, produto, cliente);
                scoresProdutosVenda.put(produtoId, score);

                ProdutoPontuacao dados = pontuacaoProdutos.computeIfAbsent(produtoId, k -> new ProdutoPontuacao());
                dados.adicionar(score, quantidade, produto);
            }

            for (VendaServico vendaServico : servicosElegiveis) {
                Servico servico = vendaServico.getServico();
                Integer servicoId = servico.getId();
                double quantidade = Optional.ofNullable(vendaServico.getQuantidade()).orElse(0);
                double base = 1.0 + quantidade + itensClienteNaVenda;
                double score = base * fatorRecencia;
                score = ajustarScoreServico(score, servico);
                scoresServicosVenda.put(servicoId, score);
            }

            if (scoresProdutosVenda.isEmpty() || scoresServicosVenda.isEmpty()) {
                continue;
            }

            for (VendaProduto vendaProduto : produtosElegiveis) {
                Produto produto = vendaProduto.getProduto();
                Integer produtoId = produto.getId();
                Double scoreProduto = scoresProdutosVenda.get(produtoId);
                if (scoreProduto == null) {
                    continue;
                }
                for (VendaServico vendaServico : servicosElegiveis) {
                    Servico servico = vendaServico.getServico();
                    Integer servicoId = servico.getId();
                    Double scoreServico = scoresServicosVenda.get(servicoId);
                    if (scoreServico == null) {
                        continue;
                    }

                    double scoreCombo = scoreProduto + scoreServico;
                    if (produtosCompativeis.contains(produtoId)) {
                        scoreCombo += COMPATIBILIDADE_BONUS;
                    }

                    BundleKey key = new BundleKey(produtoId, servicoId);
                    BundlePontuacao dadosBundle = pontuacaoBundles.computeIfAbsent(key, k -> new BundlePontuacao());
                    dadosBundle.adicionar(produto, servico, scoreCombo);
                }
            }
        }

        if (pontuacaoProdutos.isEmpty() && pontuacaoBundles.isEmpty()) {
            if (apenasBundlesFlag) {
                return Collections.emptyList();
            }
            if (!produtosCompativeis.isEmpty()) {
                return carregarProdutosPorPrioridade(produtosCompativeis, filtrarEstoquePositivo, organizationId, produtosEmRisco)
                        .stream()
                        .map(produto -> RecommendationSuggestionResponse.builder()
                                .bundle(false)
                                .score(1.0)
                                .produtos(List.of(produto))
                                .servicos(Collections.emptyList())
                                .build())
                        .toList();
            }
            return Collections.emptyList();
        }

        double rotatividadeWeight = Math.max(0.0, recommendationProperties.getRotatividadeWeight());
        double produtoMargemWeight = Math.max(0.0, recommendationProperties.getProdutoMargemWeight());
        double servicoMargemWeight = Math.max(0.0, recommendationProperties.getServicoMargemWeight());
        double bundleWeight = Math.max(1.0, recommendationProperties.getBundleWeight());

        Map<Integer, Double> pontuacaoFinalProdutos = pontuacaoProdutos.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().calcularPontuacaoFinal(rotatividadeWeight) * produtoMargemWeight));

        List<Integer> produtosOrdenados = pontuacaoFinalProdutos.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<Map.Entry<BundleKey, Double>> bundlesOrdenados = pontuacaoBundles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().calcularPontuacaoFinal(bundleWeight, produtoMargemWeight, servicoMargemWeight)))
                .entrySet().stream()
                .sorted(Map.Entry.<BundleKey, Double>comparingByValue().reversed())
                .toList();

        int limiteProdutosBusca = Math.min(produtosOrdenados.size(), MAX_SUGESTOES * CANDIDATOS_MULTIPLICADOR);
        int limiteBundlesBusca = Math.min(bundlesOrdenados.size(), MAX_SUGESTOES * CANDIDATOS_MULTIPLICADOR);

        Set<Integer> idsProdutosParaBusca = new LinkedHashSet<>(produtosOrdenados.stream()
                .limit(limiteProdutosBusca)
                .toList());
        Set<Integer> idsServicosParaBusca = new LinkedHashSet<>();

        for (int i = 0; i < limiteBundlesBusca; i++) {
            BundleKey key = bundlesOrdenados.get(i).getKey();
            idsProdutosParaBusca.add(key.produtoId());
            idsServicosParaBusca.add(key.servicoId());
        }

        List<Produto> produtos = idsProdutosParaBusca.isEmpty()
                ? Collections.emptyList()
                : produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(new ArrayList<>(idsProdutosParaBusca), organizationId);

        Map<Integer, Produto> produtosPorId = produtos.stream()
                .filter(produto -> produto.getId() != null)
                .collect(Collectors.toMap(Produto::getId, Function.identity(), (a, b) -> a));

        List<Servico> servicos = idsServicosParaBusca.isEmpty()
                ? Collections.emptyList()
                : servicoRepository.findAllById(idsServicosParaBusca);

        Map<Integer, Servico> servicosPorId = servicos.stream()
                .filter(servico -> servico.getId() != null)
                .filter(servico -> Objects.equals(servico.getOrganizationId(), organizationId))
                .collect(Collectors.toMap(Servico::getId, Function.identity(), (a, b) -> a));

        List<RecommendationSuggestionResponse> sugestoesProdutos = new ArrayList<>();
        if (!apenasBundlesFlag) {
            for (int i = 0; i < limiteProdutosBusca && i < produtosOrdenados.size(); i++) {
                Integer produtoId = produtosOrdenados.get(i);
                Produto produto = produtosPorId.get(produtoId);
                if (produto == null) {
                    continue;
                }
                if (filtrarEstoquePositivo && !estoqueDisponivel(produto)) {
                    continue;
                }
                if (produtosEmRisco.contains(produtoId)) {
                    continue;
                }
                double score = pontuacaoFinalProdutos.getOrDefault(produtoId, 0.0);
                sugestoesProdutos.add(RecommendationSuggestionResponse.builder()
                        .bundle(false)
                        .score(score)
                        .produtos(List.of(toResponse(produto)))
                        .servicos(Collections.emptyList())
                        .build());
            }
        }

        List<RecommendationSuggestionResponse> sugestoesBundles = new ArrayList<>();
        for (int i = 0; i < limiteBundlesBusca; i++) {
            Map.Entry<BundleKey, Double> entry = bundlesOrdenados.get(i);
            BundleKey key = entry.getKey();
            Produto produto = produtosPorId.get(key.produtoId());
            Servico servico = servicosPorId.get(key.servicoId());
            if (produto == null || servico == null) {
                continue;
            }
            if (filtrarEstoquePositivo && !estoqueDisponivel(produto)) {
                continue;
            }
            if (!servicoDisponivel(servico)) {
                continue;
            }
            if (produtosEmRisco.contains(produto.getId())) {
                continue;
            }
            double score = entry.getValue();
            sugestoesBundles.add(RecommendationSuggestionResponse.builder()
                    .bundle(true)
                    .score(score)
                    .produtos(List.of(toResponse(produto)))
                    .servicos(List.of(toResponse(servico)))
                    .build());
        }

        List<RecommendationSuggestionResponse> candidatas = new ArrayList<>(sugestoesBundles);
        if (!apenasBundlesFlag) {
            candidatas.addAll(sugestoesProdutos);
        }

        if (candidatas.isEmpty()) {
            return Collections.emptyList();
        }

        return candidatas.stream()
                .sorted(Comparator.comparingDouble(RecommendationSuggestionResponse::getScore).reversed())
                .limit(MAX_SUGESTOES)
                .toList();
    }

    private List<ProdutoResponse> carregarProdutosPorPrioridade(Set<Integer> idsProdutos,
                                                                boolean filtrarEstoquePositivo,
                                                                Integer organizationId,
                                                                Set<Integer> produtosEmRisco) {
        if (idsProdutos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Produto> produtos = produtoRepository
                .findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(new ArrayList<>(idsProdutos), organizationId);

        return produtos.stream()
                .filter(produto -> !filtrarEstoquePositivo || estoqueDisponivel(produto))
                .filter(produto -> !produtosEmRisco.contains(produto.getId()))
                .sorted(Comparator.comparingDouble(this::calcularPrioridadeCompatibilidade).reversed())
                .limit(MAX_SUGESTOES)
                .map(this::toResponse)
                .toList();
    }

    private double calcularPrioridadeCompatibilidade(Produto produto) {
        return calcularMargemPercentual(produto) + calcularMargemAbsoluta(produto);
    }

    private Set<Integer> identificarProdutosEmRisco(Integer organizationId) {
        try {
            List<InventoryAlert> alertas = inventoryMonitoringService.listarAlertasOrganizacao(organizationId);
            if (alertas == null || alertas.isEmpty()) {
                return Collections.emptySet();
            }
            return alertas.stream()
                    .map(InventoryAlert::getProduto)
                    .filter(Objects::nonNull)
                    .map(Produto::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (RuntimeException ignored) {
            return Collections.emptySet();
        }
    }

    private boolean estoqueDisponivel(Produto produto) {
        return Optional.ofNullable(produto.getQtdEstoque()).orElse(0) > 0;
    }

    private Cliente buscarCliente(Integer clienteId, Integer organizationId) {
        if (clienteId == null) {
            return null;
        }
        return clienteRepository.findByIdAndOrganizationId(clienteId, organizationId)
                .filter(cliente -> Boolean.TRUE.equals(cliente.getAtivo()))
                .orElse(null);
    }

    private List<Object[]> buscarHistoricoBase(Integer clienteId, Integer organizationId, boolean clienteEncontrado) {
        if (clienteId != null && clienteEncontrado) {
            return vendaRepository.findTopProdutosByCliente(clienteId, organizationId);
        }
        return vendaRepository.findTopProdutosByOrganization(organizationId);
    }

    private LocalDate calcularDataCorte() {
        int janela = Math.max(0, recommendationProperties.getHistoryWindowDays());
        if (janela <= 0) {
            return null;
        }
        return LocalDate.now().minusDays(janela);
    }

    private double ajustarScoreCliente(double score, Produto produto, Cliente cliente) {
        if (cliente == null) {
            return score;
        }

        BigDecimal churnScore = cliente.getChurnScore();
        if (churnScore != null && churnScore.compareTo(BigDecimal.ZERO) > 0) {
            double pesoChurn = Math.max(0.0, recommendationProperties.getChurnWeight());
            double churnValor = churnScore.doubleValue();
            score *= (1.0 + Math.min(churnValor, 1.0) * pesoChurn);
        }

        BigDecimal averageTicket = cliente.getAverageTicket();
        BigDecimal valorVenda = produto.getValorVenda();
        if (averageTicket != null && valorVenda != null &&
                averageTicket.compareTo(BigDecimal.ZERO) > 0 && valorVenda.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal proporcao = averageTicket.divide(valorVenda, 4, RoundingMode.HALF_UP);
            double fatorTicket = Math.max(0.25, Math.min(1.0, proporcao.doubleValue()));
            score *= fatorTicket;
        }

        return score;
    }

    private double ajustarScoreServico(double score, Servico servico) {
        if (servico == null) {
            return score;
        }

        BigDecimal valorVenda = servico.getValorVenda();
        BigDecimal custo = servico.getCusto();
        if (valorVenda != null && custo != null && valorVenda.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margem = valorVenda.subtract(custo);
            if (margem.compareTo(BigDecimal.ZERO) > 0) {
                double percentual = margem.divide(valorVenda, 4, RoundingMode.HALF_UP).doubleValue();
                score *= (1.0 + Math.min(percentual, 1.0));
            }
        }

        return score;
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

    private ServicoResponse toResponse(Servico servico) {
        return ServicoResponse.builder()
                .id(servico.getId())
                .organizationId(servico.getOrganizationId())
                .fornecedorId(servico.getFornecedorId())
                .sequencialUsuario(servico.getSequencialUsuario())
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .custo(servico.getCusto())
                .disponivelVenda(servico.getDisponivelVenda())
                .valorVenda(servico.getValorVenda())
                .tempoExecucao(servico.getTempoExecucao())
                .terceirizado(servico.getTerceirizado())
                .ativo(servico.getAtivo())
                .build();
    }

    private boolean servicoDisponivel(Servico servico) {
        if (servico == null) {
            return false;
        }
        if (!Objects.equals(servico.getOrganizationId(), CurrentUser.getOrganizationId())) {
            return false;
        }
        return Boolean.TRUE.equals(servico.getAtivo()) && Boolean.TRUE.equals(servico.getDisponivelVenda());
    }

    private static double calcularMargemPercentual(Produto produto) {
        BigDecimal valorVenda = produto.getValorVenda();
        BigDecimal custo = produto.getCusto();
        if (valorVenda == null || custo == null || valorVenda.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal margem = valorVenda.subtract(custo);
        if (margem.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return margem.divide(valorVenda, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static double calcularMargemAbsoluta(Produto produto) {
        BigDecimal valorVenda = produto.getValorVenda();
        BigDecimal custo = produto.getCusto();
        if (valorVenda == null || custo == null) {
            return 0.0;
        }
        BigDecimal margem = valorVenda.subtract(custo);
        return margem.max(BigDecimal.ZERO).doubleValue();
    }

    private static double calcularMargemPercentual(Servico servico) {
        BigDecimal valorVenda = servico.getValorVenda();
        BigDecimal custo = servico.getCusto();
        if (valorVenda == null || custo == null || valorVenda.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal margem = valorVenda.subtract(custo);
        if (margem.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return margem.divide(valorVenda, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static double calcularMargemAbsoluta(Servico servico) {
        BigDecimal valorVenda = servico.getValorVenda();
        BigDecimal custo = servico.getCusto();
        if (valorVenda == null || custo == null) {
            return 0.0;
        }
        BigDecimal margem = valorVenda.subtract(custo);
        return margem.max(BigDecimal.ZERO).doubleValue();
    }

    private static double normalizarMargemAbsoluta(double margemAbsoluta) {
        if (margemAbsoluta <= 0) {
            return 0.0;
        }
        return Math.min(0.5, margemAbsoluta / 100.0);
    }

    private static class ProdutoPontuacao {
        private double scoreAcumulado;
        private int recorrencia;
        private double melhorMargemPercentual;
        private double melhorMargemAbsoluta;
        private double melhorRotatividade;

        private ProdutoPontuacao() {
            this.scoreAcumulado = 0.0;
            this.recorrencia = 0;
            this.melhorMargemPercentual = 0.0;
            this.melhorMargemAbsoluta = 0.0;
            this.melhorRotatividade = 0.0;
        }

        private void adicionar(double score, double quantidade, Produto produto) {
            this.scoreAcumulado += score;
            this.recorrencia += Math.max(1, (int) Math.round(quantidade));
            this.melhorMargemPercentual = Math.max(this.melhorMargemPercentual, RecommendationService.calcularMargemPercentual(produto));
            this.melhorMargemAbsoluta = Math.max(this.melhorMargemAbsoluta, RecommendationService.calcularMargemAbsoluta(produto));
            BigDecimal rotatividade = produto.getRotatividade();
            if (rotatividade != null) {
                this.melhorRotatividade = Math.max(this.melhorRotatividade, rotatividade.doubleValue());
            }
        }

        private double calcularPontuacaoFinal(double rotatividadeWeight) {
            double fatorRecorrencia = 1.0 + Math.log1p(recorrencia);
            double fatorMargem = 1.0 + melhorMargemPercentual + RecommendationService.normalizarMargemAbsoluta(melhorMargemAbsoluta);
            double fatorRotatividade = calcularFatorRotatividade(rotatividadeWeight);
            return scoreAcumulado * fatorRecorrencia * fatorMargem * fatorRotatividade;
        }

        private double calcularFatorRotatividade(double rotatividadeWeight) {
            if (rotatividadeWeight <= 0 || melhorRotatividade <= 0) {
                return 1.0;
            }

            if (melhorRotatividade < 0.5) {
                double deficit = 0.5 - melhorRotatividade;
                return 1.0 + rotatividadeWeight * Math.min(1.5, 1.0 + deficit);
            }

            if (melhorRotatividade > 1.5) {
                return 1.0 + rotatividadeWeight * Math.min(1.0, melhorRotatividade - 1.0);
            }

            return 1.0 + rotatividadeWeight;
        }
    }

    private static final class BundleKey {
        private final Integer produtoId;
        private final Integer servicoId;

        private BundleKey(Integer produtoId, Integer servicoId) {
            this.produtoId = produtoId;
            this.servicoId = servicoId;
        }

        private Integer produtoId() {
            return produtoId;
        }

        private Integer servicoId() {
            return servicoId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BundleKey bundleKey = (BundleKey) o;
            return Objects.equals(produtoId, bundleKey.produtoId) && Objects.equals(servicoId, bundleKey.servicoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(produtoId, servicoId);
        }
    }

    private static class BundlePontuacao {
        private double scoreAcumulado;
        private int recorrencia;
        private double melhorMargemProdutoPercentual;
        private double melhorMargemProdutoAbsoluta;
        private double melhorMargemServicoPercentual;
        private double melhorMargemServicoAbsoluta;

        private void adicionar(Produto produto, Servico servico, double scoreCombo) {
            this.scoreAcumulado += scoreCombo;
            this.recorrencia++;
            this.melhorMargemProdutoPercentual = Math.max(this.melhorMargemProdutoPercentual,
                    RecommendationService.calcularMargemPercentual(produto));
            this.melhorMargemProdutoAbsoluta = Math.max(this.melhorMargemProdutoAbsoluta,
                    RecommendationService.calcularMargemAbsoluta(produto));
            this.melhorMargemServicoPercentual = Math.max(this.melhorMargemServicoPercentual,
                    RecommendationService.calcularMargemPercentual(servico));
            this.melhorMargemServicoAbsoluta = Math.max(this.melhorMargemServicoAbsoluta,
                    RecommendationService.calcularMargemAbsoluta(servico));
        }

        private double calcularPontuacaoFinal(double bundleWeight, double produtoMargemWeight, double servicoMargemWeight) {
            if (recorrencia <= 0) {
                return 0.0;
            }
            double fatorRecorrencia = 1.0 + Math.log1p(recorrencia);
            double fatorProdutoMargem = 1.0 + produtoMargemWeight * (melhorMargemProdutoPercentual
                    + RecommendationService.normalizarMargemAbsoluta(melhorMargemProdutoAbsoluta));
            double fatorServicoMargem = 1.0 + servicoMargemWeight * (melhorMargemServicoPercentual
                    + RecommendationService.normalizarMargemAbsoluta(melhorMargemServicoAbsoluta));
            double fatorBundle = Math.max(1.0, bundleWeight);
            return scoreAcumulado * fatorRecorrencia * fatorProdutoMargem * fatorServicoMargem * fatorBundle;
        }
    }
}
