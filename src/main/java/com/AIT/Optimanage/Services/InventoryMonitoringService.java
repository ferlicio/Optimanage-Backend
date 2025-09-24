package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Inventory.InventoryAction;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.Inventory.InventoryAlertSeverity;
import com.AIT.Optimanage.Models.Inventory.InventoryHistory;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Repositories.InventoryAlertRepository;
import com.AIT.Optimanage.Repositories.InventoryHistoryRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Services.PlanoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMonitoringService {

    private static final int CONSUMPTION_LOOKBACK_DAYS = 30;

    private final ProdutoRepository produtoRepository;
    private final InventoryHistoryRepository historyRepository;
    private final InventoryAlertRepository alertRepository;
    private final PlanoService planoService;
    private final Clock clock;

    @Scheduled(cron = "${inventory.monitoring.cron:0 0 6 * * *}")
    @Transactional
    public void executarAnaliseAgendada() {
        List<Produto> todosProdutos = produtoRepository.findAll();
        Map<Integer, List<Produto>> produtosPorOrganizacao = todosProdutos.stream()
                .filter(produto -> produto.getOrganizationId() != null)
                .filter(produto -> Boolean.TRUE.equals(produto.getAtivo()))
                .collect(Collectors.groupingBy(Produto::getOrganizationId));

        produtosPorOrganizacao.forEach((organizationId, produtos) -> {
            try {
                if (!planoService.isMonitoramentoEstoqueHabilitado(organizationId)) {
                    alertRepository.deleteByOrganizationId(organizationId);
                    return;
                }
                recalcularAlertas(organizationId, produtos);
            } catch (Exception e) {
                log.error("Falha ao recalcular alertas de estoque para a organização {}", organizationId, e);
            }
        });
    }

    @Transactional
    public List<InventoryAlert> recalcularAlertasOrganizacao(Integer organizationId) {
        if (!planoService.isMonitoramentoEstoqueHabilitado(organizationId)) {
            alertRepository.deleteByOrganizationId(organizationId);
            return Collections.emptyList();
        }
        List<Produto> produtos = produtoRepository.findAllByOrganizationIdAndAtivoTrue(organizationId);
        return recalcularAlertas(organizationId, produtos);
    }

    @Transactional
    public List<InventoryAlert> recalcularAlertas(Integer organizationId, List<Produto> produtos) {
        if (organizationId == null) {
            return Collections.emptyList();
        }
        if (!planoService.isMonitoramentoEstoqueHabilitado(organizationId)) {
            alertRepository.deleteByOrganizationId(organizationId);
            return Collections.emptyList();
        }
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime inicioJanela = hoje.minusDays(CONSUMPTION_LOOKBACK_DAYS - 1L).atStartOfDay();
        Map<Integer, List<InventoryHistory>> historicosPorProduto = carregarHistoricosConsumo(
                organizationId,
                produtos,
                inicioJanela);

        List<InventoryAlert> alertas = produtos.stream()
                .map(produto -> avaliarProduto(
                        produto,
                        hoje,
                        historicosPorProduto.getOrDefault(produto.getId(), Collections.emptyList())))
                .flatMap(Optional::stream)
                .toList();

        alertRepository.deleteByOrganizationId(organizationId);
        if (!alertas.isEmpty()) {
            alertas.forEach(alerta -> alerta.setTenantId(organizationId));
            alertRepository.saveAll(alertas);
        }
        return alertas;
    }

    @Transactional(readOnly = true)
    public List<InventoryAlert> listarAlertasOrganizacao(Integer organizationId) {
        if (!planoService.isMonitoramentoEstoqueHabilitado(organizationId)) {
            return Collections.emptyList();
        }
        return alertRepository.findByOrganizationIdOrderBySeverityDescDiasRestantesAsc(organizationId);
    }

    private Optional<InventoryAlert> avaliarProduto(Produto produto, LocalDate hoje, List<InventoryHistory> historicos) {
        Integer estoqueAtual = Optional.ofNullable(produto.getQtdEstoque()).orElse(0);
        Integer estoqueMinimo = Optional.ofNullable(produto.getEstoqueMinimo()).orElse(0);
        Integer prazoReposicao = Optional.ofNullable(produto.getPrazoReposicaoDias()).orElse(0);

        BigDecimal consumoMedioDiario = calcularConsumoMedioDiario(produto, hoje, historicos);
        Integer diasRestantes = calcularDiasRestantes(consumoMedioDiario, estoqueAtual);
        LocalDate dataRuptura = diasRestantes != null ? hoje.plusDays(diasRestantes) : null;

        InventoryAlertSeverity severity = determinarSeveridade(estoqueAtual, estoqueMinimo, prazoReposicao, diasRestantes);
        if (severity == null) {
            return Optional.empty();
        }

        int quantidadeSugerida = calcularQuantidadeSugerida(estoqueAtual, estoqueMinimo, prazoReposicao, consumoMedioDiario);
        String mensagem = construirMensagem(estoqueAtual, estoqueMinimo, prazoReposicao, diasRestantes, severity);

        InventoryAlert alerta = InventoryAlert.builder()
                .produto(produto)
                .severity(severity)
                .diasRestantes(diasRestantes)
                .consumoMedioDiario(consumoMedioDiario)
                .estoqueAtual(estoqueAtual)
                .estoqueMinimo(estoqueMinimo)
                .prazoReposicaoDias(prazoReposicao)
                .quantidadeSugerida(quantidadeSugerida)
                .dataEstimadaRuptura(dataRuptura)
                .mensagem(mensagem)
                .build();
        alerta.setTenantId(produto.getOrganizationId());
        return Optional.of(alerta);
    }

    private BigDecimal calcularConsumoMedioDiario(Produto produto, LocalDate hoje, List<InventoryHistory> historicos) {
        if (produto.getId() == null || produto.getOrganizationId() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (historicos == null || historicos.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Map<LocalDate, Integer> consumoPorDia = new HashMap<>();
        for (InventoryHistory historico : historicos) {
            if (historico.getCreatedAt() == null) {
                continue;
            }
            LocalDate data = historico.getCreatedAt().toLocalDate();
            consumoPorDia.merge(data, historico.getQuantidade(), Integer::sum);
        }
        if (consumoPorDia.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalConsumido = BigDecimal.valueOf(consumoPorDia.values().stream()
                .mapToInt(Integer::intValue)
                .sum());
        LocalDate primeiraData = consumoPorDia.keySet().stream()
                .min(LocalDate::compareTo)
                .orElse(hoje);
        long diasConsiderados = Math.max(1,
                Math.min(CONSUMPTION_LOOKBACK_DAYS, ChronoUnit.DAYS.between(primeiraData, hoje) + 1));
        return totalConsumido.divide(BigDecimal.valueOf(diasConsiderados), 2, RoundingMode.HALF_UP);
    }

    private Map<Integer, List<InventoryHistory>> carregarHistoricosConsumo(Integer organizationId,
                                                                           List<Produto> produtos,
                                                                           LocalDateTime inicioJanela) {
        if (produtos == null || produtos.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> produtoIds = produtos.stream()
                .map(Produto::getId)
                .filter(Objects::nonNull)
                .toList();
        if (produtoIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<InventoryHistory> historicos = historyRepository
                .findByOrganizationIdAndActionAndCreatedAtAfterAndProduto_IdInOrderByProduto_IdAscCreatedAtAsc(
                        organizationId,
                        InventoryAction.DECREMENT,
                        inicioJanela,
                        produtoIds);

        if (historicos.isEmpty()) {
            return Collections.emptyMap();
        }

        return historicos.stream()
                .filter(historico -> historico.getProduto() != null && historico.getProduto().getId() != null)
                .collect(Collectors.groupingBy(historico -> historico.getProduto().getId()));
    }

    private Integer calcularDiasRestantes(BigDecimal consumoMedioDiario, Integer estoqueAtual) {
        if (consumoMedioDiario == null || consumoMedioDiario.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (estoqueAtual == null) {
            return null;
        }
        return BigDecimal.valueOf(estoqueAtual)
                .divide(consumoMedioDiario, 0, RoundingMode.DOWN)
                .intValue();
    }

    private InventoryAlertSeverity determinarSeveridade(Integer estoqueAtual, Integer estoqueMinimo,
                                                        Integer prazoReposicao, Integer diasRestantes) {
        if (estoqueAtual == null) {
            return null;
        }
        if (estoqueAtual <= estoqueMinimo) {
            return InventoryAlertSeverity.CRITICAL;
        }
        if (diasRestantes == null) {
            return null;
        }
        if (prazoReposicao != null && prazoReposicao > 0 && diasRestantes <= prazoReposicao) {
            return InventoryAlertSeverity.CRITICAL;
        }
        int limiteAtencao = prazoReposicao != null && prazoReposicao > 0 ? prazoReposicao + 2 : 3;
        if (diasRestantes <= limiteAtencao) {
            return InventoryAlertSeverity.WARNING;
        }
        return null;
    }

    private int calcularQuantidadeSugerida(Integer estoqueAtual, Integer estoqueMinimo, Integer prazoReposicao,
                                           BigDecimal consumoMedioDiario) {
        int atual = Optional.ofNullable(estoqueAtual).orElse(0);
        int minimo = Optional.ofNullable(estoqueMinimo).orElse(0);
        int prazo = Optional.ofNullable(prazoReposicao).orElse(0);

        int consumoDurantePrazo = 0;
        if (consumoMedioDiario != null && consumoMedioDiario.compareTo(BigDecimal.ZERO) > 0 && prazo > 0) {
            consumoDurantePrazo = consumoMedioDiario
                    .multiply(BigDecimal.valueOf(prazo))
                    .setScale(0, RoundingMode.CEILING)
                    .intValue();
        }
        int alvo = minimo + consumoDurantePrazo;
        return Math.max(0, alvo - atual);
    }

    private String construirMensagem(Integer estoqueAtual, Integer estoqueMinimo, Integer prazoReposicao,
                                     Integer diasRestantes, InventoryAlertSeverity severity) {
        if (estoqueAtual != null && estoqueAtual <= estoqueMinimo) {
            return "Estoque abaixo do mínimo definido.";
        }
        if (diasRestantes == null) {
            return "Consumo insuficiente para projeção de ruptura.";
        }
        if (severity == InventoryAlertSeverity.CRITICAL && prazoReposicao != null && prazoReposicao > 0) {
            return String.format("Estoque projetado para acabar em %d dias (prazo de reposição: %d).",
                    diasRestantes, prazoReposicao);
        }
        return String.format("Estoque projetado para acabar em %d dias.", diasRestantes);
    }
}
