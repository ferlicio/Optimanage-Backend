package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Repositories.Venda.projection.ProdutoQuantidadeProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoMetricsService {

    private static final int LOOKBACK_DAYS = 90;

    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;
    private final PlanoService planoService;
    private final Clock clock;

    @Scheduled(cron = "${product.metrics.cron:0 0 5 * * SAT,SUN}")
    @Transactional
    public void atualizarMetricasAgendado() {
        List<Produto> todosProdutos = produtoRepository.findAll();
        Map<Integer, List<Produto>> produtosPorOrganizacao = todosProdutos.stream()
                .filter(produto -> produto.getOrganizationId() != null)
                .filter(produto -> Boolean.TRUE.equals(produto.getAtivo()))
                .collect(Collectors.groupingBy(Produto::getOrganizationId));

        produtosPorOrganizacao.forEach((organizationId, produtos) -> {
            try {
                if (!planoService.isMetricasProdutoHabilitadas(organizationId)) {
                    return;
                }
                recalcularRotatividade(organizationId, produtos);
            } catch (Exception ex) {
                log.error("Falha ao atualizar rotatividade dos produtos para a organização {}", organizationId, ex);
            }
        });
    }

    @Transactional
    public void recalcularRotatividade(Integer organizationId, List<Produto> produtos) {
        if (organizationId == null || produtos == null || produtos.isEmpty()) {
            return;
        }
        if (!planoService.isMetricasProdutoHabilitadas(organizationId)) {
            return;
        }

        LocalDate fim = LocalDate.now(clock);
        LocalDate inicio = fim.minusDays(LOOKBACK_DAYS - 1L);

        Map<Integer, Long> quantidadeVendidaPorProduto = vendaRepository
                .sumQuantidadeVendidaPorProduto(organizationId, inicio, fim)
                .stream()
                .collect(Collectors.toMap(ProdutoQuantidadeProjection::getProdutoId,
                        ProdutoQuantidadeProjection::getTotalQuantidade));

        produtos.stream()
                .filter(produto -> produto.getId() != null)
                .forEach(produto -> {
                    long quantidadeVendida = quantidadeVendidaPorProduto.getOrDefault(produto.getId(), 0L);
                    BigDecimal rotatividade = calcularRotatividade(produto, quantidadeVendida);
                    produto.setRotatividade(rotatividade);
                });

        produtoRepository.saveAll(produtos);
    }

    private BigDecimal calcularRotatividade(Produto produto, long quantidadeVendida) {
        if (quantidadeVendida <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        int estoqueAtual = Optional.ofNullable(produto.getQtdEstoque()).orElse(0);
        BigDecimal vendidos = BigDecimal.valueOf(quantidadeVendida);
        BigDecimal estoqueMedio = vendidos
                .add(BigDecimal.valueOf(estoqueAtual))
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        if (estoqueMedio.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return vendidos.divide(estoqueMedio, 4, RoundingMode.HALF_UP);
    }
}
