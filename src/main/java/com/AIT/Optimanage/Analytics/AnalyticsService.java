package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.InventoryAlertDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformEngajamentoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformFeatureAdoptionDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformHealthScoreDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformOrganizationsResumoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformResumoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationPlanFinancialProjection;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.Organization.PlanFeatureAdoptionProjection;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.InventoryMonitoringService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Support.PlatformConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final CompraRepository compraRepository;
    private final InventoryMonitoringService inventoryMonitoringService;
    private final PlanoService planoService;
    private final OrganizationRepository organizationRepository;

    public ResumoDTO obterResumo() {
        Organization organization = getCurrentOrganizationOrThrow();
        Integer organizationId = organization.getId();
        BigDecimal totalVendas = BigDecimal.ZERO;
        BigDecimal vendasResult = vendaRepository.sumValorFinalByOrganization(organizationId);
        if (vendasResult != null) {
            totalVendas = vendasResult;
        }

        BigDecimal totalCompras = BigDecimal.ZERO;
        BigDecimal comprasResult = compraRepository.sumValorFinalByOrganization(organizationId);
        if (comprasResult != null) {
            totalCompras = comprasResult;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
        LocalDate inicioAno = hoje.withDayOfYear(1);
        LocalDate fimAno = hoje.withDayOfYear(hoje.lengthOfYear());

        BigDecimal vendasMensais = vendaRepository.sumValorFinalByOrganizationBetweenDates(organizationId, inicioMes, fimMes);
        BigDecimal vendasAnuais = vendaRepository.sumValorFinalByOrganizationBetweenDates(organizationId, inicioAno, fimAno);

        BigDecimal metaMensal = organization.getMetaMensal() != null
                ? BigDecimal.valueOf(organization.getMetaMensal())
                : null;
        BigDecimal metaAnual = organization.getMetaAnual() != null
                ? BigDecimal.valueOf(organization.getMetaAnual())
                : null;

        BigDecimal progressoMensal = calcularProgresso(vendasMensais, metaMensal);
        BigDecimal progressoAnual = calcularProgresso(vendasAnuais, metaAnual);

        BigDecimal lucro = totalVendas.subtract(totalCompras);
        return new ResumoDTO(totalVendas, totalCompras, lucro, metaMensal, metaAnual, progressoMensal, progressoAnual);
    }

    public PrevisaoDTO preverDemanda() {
        Organization organization = getCurrentOrganizationOrThrow();
        Integer organizationId = organization.getId();
        List<Venda> vendas = vendaRepository.findAll().stream()
                .filter(v -> organizationId.equals(v.getOrganizationId()))
                .filter(v -> v.getDataEfetuacao() != null && v.getValorFinal() != null)
                .sorted(Comparator.comparing(Venda::getDataEfetuacao))
                .toList();

        if (vendas.isEmpty()) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        // Aggregate sales totals per day to avoid bias when multiple sales happen on the same date.
        var dailyTotals = vendas.stream()
                .collect(Collectors.toMap(
                        Venda::getDataEfetuacao,
                        Venda::getValorFinal,
                        BigDecimal::add,
                        java.util.TreeMap::new
                ));

        if (dailyTotals.size() < 2) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        // Forecast using a weighted linear regression.
        // This placeholder approach can be replaced by an AI-based model in the future.
        var dailyEntries = dailyTotals.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .toList();

        long baseDay = dailyEntries.get(0).getKey().toEpochDay();
        int lastIndex = dailyEntries.size() - 1;
        double sumWeights = 0d;
        double sumWeightedX = 0d;
        double sumWeightedY = 0d;
        double sumWeightedXX = 0d;
        double sumWeightedXY = 0d;
        for (int index = 0; index < dailyEntries.size(); index++) {
            var entry = dailyEntries.get(index);
            double x = entry.getKey().toEpochDay() - baseDay;
            double y = entry.getValue().doubleValue();
            double weight = lastIndex == 0 ? 1.0 : 1.0 + (double) index / lastIndex;
            sumWeights += weight;
            sumWeightedX += weight * x;
            sumWeightedY += weight * y;
            sumWeightedXX += weight * x * x;
            sumWeightedXY += weight * x * y;
        }

        double nextDay = dailyEntries.get(lastIndex).getKey().toEpochDay() - baseDay + 1;
        double denominator = (sumWeights * sumWeightedXX) - (sumWeightedX * sumWeightedX);
        if (Math.abs(denominator) < 1e-9) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        double slope = ((sumWeights * sumWeightedXY) - (sumWeightedX * sumWeightedY)) / denominator;
        double intercept = (sumWeightedY - slope * sumWeightedX) / sumWeights;
        double forecast = intercept + (slope * nextDay);
        if (Double.isNaN(forecast) || Double.isInfinite(forecast)) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        double nonNegativeForecast = Math.max(forecast, 0d);
        return new PrevisaoDTO(BigDecimal.valueOf(nonNegativeForecast));
    }

    public List<InventoryAlertDTO> listarAlertasEstoque() {
        Organization organization = getCurrentOrganizationOrThrow();
        Integer organizationId = organization.getId();
        if (!planoService.isMonitoramentoEstoqueHabilitado(organizationId)) {
            throw new AccessDeniedException("Monitoramento de estoque não está habilitado no plano atual");
        }
        List<InventoryAlert> alertas = inventoryMonitoringService.recalcularAlertasOrganizacao(organizationId);
        if (alertas.isEmpty()) {
            return List.of();
        }
        return alertas.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Organization requirePlatformOrganization() {
        Organization organization = getCurrentOrganizationOrThrow();
        if (!isPlatformOrganization(organization)) {
            throw new AccessDeniedException("Acesso restrito à organização da plataforma");
        }
        return organization;
    }

    public PlatformOrganizationsResumoDTO obterResumoPlataforma() {
        requirePlatformOrganization();

        LocalDate hoje = LocalDate.now();
        LocalDate inicioJanela = hoje.minusDays(29);
        LocalDateTime inicioJanelaDateTime = inicioJanela.atStartOfDay();
        LocalDateTime fimJanelaDateTime = hoje.atTime(LocalTime.MAX);

        List<Object[]> criadasBruto = organizationRepository.countOrganizationsCreatedByDateRange(
                inicioJanelaDateTime,
                fimJanelaDateTime,
                PlatformConstants.PLATFORM_ORGANIZATION_ID
        );
        List<Object[]> assinadasBruto = organizationRepository.countOrganizationsSignedByDateRange(
                inicioJanela,
                hoje,
                PlatformConstants.PLATFORM_ORGANIZATION_ID
        );

        Map<LocalDate, Long> criadasPorDia = toDailyCountMap(criadasBruto);
        Map<LocalDate, Long> ativadasPorDia = toDailyCountMap(assinadasBruto);

        List<PlatformOrganizationsResumoDTO.TimeSeriesPoint> criadasSerie = buildTimeSeries(
                inicioJanela,
                hoje,
                criadasPorDia
        );
        List<PlatformOrganizationsResumoDTO.TimeSeriesPoint> ativadasSerie = buildTimeSeries(
                inicioJanela,
                hoje,
                ativadasPorDia
        );

        long totalAtivas = organizationRepository.countOrganizationsActiveByDateRange(
                inicioJanela,
                hoje,
                PlatformConstants.PLATFORM_ORGANIZATION_ID
        );
        long totalOrganizacoes = organizationRepository.countAllExcluding(PlatformConstants.PLATFORM_ORGANIZATION_ID);
        long totalInativas = Math.max(totalOrganizacoes - totalAtivas, 0);

        return PlatformOrganizationsResumoDTO.builder()
                .criadas(criadasSerie)
                .ativadas(ativadasSerie)
                .totalAtivas(totalAtivas)
                .totalInativas(totalInativas)
                .build();
    }

    public PlatformResumoDTO obterResumoFinanceiroPlataforma() {
        requirePlatformOrganization();

        BigDecimal totalVendas = defaultZero(vendaRepository.sumValorFinalGlobal(null));
        BigDecimal totalCompras = defaultZero(compraRepository.sumValorFinalGlobal(null));
        BigDecimal lucro = totalVendas.subtract(totalCompras);

        long quantidadeVendas = vendaRepository.countByOrganizationOrGlobal(null);
        BigDecimal ticketMedio = quantidadeVendas > 0
                ? totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        LocalDate inicioPeriodo = mesAnterior.atDay(1);
        LocalDate fimPeriodo = mesAtual.atEndOfMonth();

        List<Object[]> totaisMensais = vendaRepository.sumValorFinalByMonthGlobal(null, inicioPeriodo, fimPeriodo);
        BigDecimal crescimentoMensal = calcularCrescimentoMensal(totaisMensais, mesAtual, mesAnterior);

        List<OrganizationPlanFinancialProjection> planosFinanceiros = organizationRepository
                .aggregatePlanFinancials(PlatformConstants.PLATFORM_ORGANIZATION_ID);

        List<PlatformResumoDTO.PlanoReceitaDTO> receitaPorPlano = new ArrayList<>();
        BigDecimal receitaRecorrenteMensal = BigDecimal.ZERO;
        BigDecimal receitaRecorrenteAnual = BigDecimal.ZERO;

        for (OrganizationPlanFinancialProjection projection : planosFinanceiros) {
            BigDecimal valorPlano = toBigDecimal(projection.getPlanValue());
            Integer duracaoPlano = projection.getPlanDurationDays();
            long quantidadeOrganizacoes = projection.getOrganizationCount() != null
                    ? projection.getOrganizationCount()
                    : 0L;

            BigDecimal receitaMensalPorOrganizacao = calcularReceitaMensalRecorrente(valorPlano, duracaoPlano);
            BigDecimal receitaMensalPlano = receitaMensalPorOrganizacao.multiply(BigDecimal.valueOf(quantidadeOrganizacoes));
            BigDecimal receitaAnualPlano = receitaMensalPlano.multiply(BigDecimal.valueOf(12));

            receitaRecorrenteMensal = receitaRecorrenteMensal.add(receitaMensalPlano);
            receitaRecorrenteAnual = receitaRecorrenteAnual.add(receitaAnualPlano);

            receitaPorPlano.add(PlatformResumoDTO.PlanoReceitaDTO.builder()
                    .planoId(projection.getPlanId())
                    .planoNome(projection.getPlanName())
                    .quantidadeOrganizacoes(quantidadeOrganizacoes)
                    .receitaRecorrenteMensal(receitaMensalPlano.setScale(2, RoundingMode.HALF_UP))
                    .receitaRecorrenteAnual(receitaAnualPlano.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return PlatformResumoDTO.builder()
                .volumeTotalVendas(totalVendas)
                .volumeTotalCompras(totalCompras)
                .lucroAgregado(lucro)
                .ticketMedio(ticketMedio)
                .crescimentoMensal(crescimentoMensal)
                .receitaRecorrenteMensal(receitaRecorrenteMensal.setScale(2, RoundingMode.HALF_UP))
                .receitaRecorrenteAnual(receitaRecorrenteAnual.setScale(2, RoundingMode.HALF_UP))
                .receitaPorPlano(receitaPorPlano)
                .build();
    }
  

    public PlatformEngajamentoDTO obterEngajamentoPlataforma() {
        requirePlatformOrganization();

        LocalDate hoje = LocalDate.now();
        LocalDate corte30Dias = hoje.minusDays(30);
        LocalDate corte60Dias = hoje.minusDays(60);

        long organizacoesAtivas30Dias = vendaRepository.countDistinctOrganizationsByPeriodo(corte30Dias, hoje);
        long organizacoesAtivas60Dias = vendaRepository.countDistinctOrganizationsByPeriodo(corte60Dias, hoje);

        long organizacoesInativas60Dias = organizationRepository
                .countOrganizationsWithoutSalesSince(corte60Dias, PlatformConstants.PLATFORM_ORGANIZATION_ID);

        Map<Integer, LocalDate> primeirasVendas = vendaRepository.findPrimeiraVendaPorOrganizacao(null, hoje)
                .stream()
                .filter(Objects::nonNull)
                .filter(linha -> linha.length >= 2 && linha[0] instanceof Integer && linha[1] instanceof LocalDate)
                .collect(Collectors.toMap(
                        linha -> (Integer) linha[0],
                        linha -> (LocalDate) linha[1],
                        (existing, ignored) -> existing
                ));

        BigDecimal tempoMedioPrimeiraVenda = calcularTempoMedioPrimeiraVenda(primeirasVendas);
        long totalOrganizacoesComVendas = primeirasVendas.size();

        BigDecimal taxaRetencao30Dias = calcularTaxaRetencao(organizacoesAtivas30Dias, totalOrganizacoesComVendas);
        BigDecimal taxaRetencao60Dias = calcularTaxaRetencao(organizacoesAtivas60Dias, totalOrganizacoesComVendas);

        return PlatformEngajamentoDTO.builder()
                .organizacoesAtivas30Dias(organizacoesAtivas30Dias)
                .organizacoesAtivas60Dias(organizacoesAtivas60Dias)
                .organizacoesInativas60Dias(organizacoesInativas60Dias)
                .tempoMedioPrimeiraVendaDias(tempoMedioPrimeiraVenda)
                .taxaRetencao30Dias(taxaRetencao30Dias)
                .taxaRetencao60Dias(taxaRetencao60Dias)
                .build();
    }


    public PlatformHealthScoreDTO obterHealthScorePlataforma() {
        requirePlatformOrganization();

        LocalDate hoje = LocalDate.now();
        LocalDate corte30Dias = hoje.minusDays(30);
        LocalDate corte60Dias = hoje.minusDays(60);
        LocalDate corte90Dias = hoje.minusDays(90);

        Set<Integer> todasOrganizacoes = organizationRepository.findAll().stream()
                .map(Organization::getId)
                .filter(Objects::nonNull)
                .filter(id -> !PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(id))
                .collect(Collectors.toSet());

        Set<Integer> organizacoesComVendas30Dias = new HashSet<>(sanitizeOrganizationIds(
                vendaRepository.findDistinctOrganizationIdsWithSalesBetween(corte30Dias, hoje)
        ));
        organizacoesComVendas30Dias.retainAll(todasOrganizacoes);

        Set<Integer> organizacoesComCompras30Dias = new HashSet<>(sanitizeOrganizationIds(
                compraRepository.findDistinctOrganizationIdsWithPurchasesBetween(corte30Dias, hoje)
        ));
        organizacoesComCompras30Dias.retainAll(todasOrganizacoes);

        Set<Integer> organizacoesComVendas60Dias = new HashSet<>(sanitizeOrganizationIds(
                vendaRepository.findDistinctOrganizationIdsWithSalesBetween(corte60Dias, hoje)
        ));
        organizacoesComVendas60Dias.retainAll(todasOrganizacoes);

        Set<Integer> organizacoesComCompras90Dias = new HashSet<>(sanitizeOrganizationIds(
                compraRepository.findDistinctOrganizationIdsWithPurchasesBetween(corte90Dias, hoje)
        ));
        organizacoesComCompras90Dias.retainAll(todasOrganizacoes);

        Set<Integer> ativoEmVendas = new HashSet<>(organizacoesComVendas30Dias);

        Set<Integer> ativoEmCompras = new HashSet<>(organizacoesComCompras30Dias);
        ativoEmCompras.removeAll(ativoEmVendas);

        Set<Integer> semVendas60Dias = new HashSet<>(todasOrganizacoes);
        semVendas60Dias.removeAll(organizacoesComVendas60Dias);

        Set<Integer> emRisco = new HashSet<>(semVendas60Dias);
        emRisco.retainAll(organizacoesComCompras90Dias);
        emRisco.removeAll(ativoEmCompras);
        emRisco.removeAll(ativoEmVendas);

        Set<Integer> churn = new HashSet<>(todasOrganizacoes);
        churn.removeAll(ativoEmVendas);
        churn.removeAll(ativoEmCompras);
        churn.removeAll(emRisco);
        churn.removeAll(organizacoesComCompras90Dias);

        long totalOrganizacoes = todasOrganizacoes.size();
        long clientesSemVendasRecentes = Math.max(totalOrganizacoes - ativoEmVendas.size(), 0);

        BigDecimal volumeCompras30Dias = defaultZero(
                compraRepository.sumValorFinalGlobalBetweenDates(
                        corte30Dias,
                        hoje,
                        PlatformConstants.PLATFORM_ORGANIZATION_ID
                )
        ).setScale(2, RoundingMode.HALF_UP);

        return PlatformHealthScoreDTO.builder()
                .totalOrganizations(totalOrganizacoes)
                .clientesSemVendasRecentes(clientesSemVendasRecentes)
                .volumeComprasUltimos30Dias(volumeCompras30Dias)
                .ativoEmVendas(buildHealthSegment(ativoEmVendas.size(), totalOrganizacoes))
                .ativoEmCompras(buildHealthSegment(ativoEmCompras.size(), totalOrganizacoes))
                .emRisco(buildHealthSegment(emRisco.size(), totalOrganizacoes))
                .churn(buildHealthSegment(churn.size(), totalOrganizacoes))
                .build();
    }


    public PlatformFeatureAdoptionDTO obterAdocaoRecursosPlataforma() {
        requirePlatformOrganization();

        List<PlanFeatureAdoptionProjection> aggregations = organizationRepository
                .aggregateFeatureAdoptionByPlan(PlatformConstants.PLATFORM_ORGANIZATION_ID);

        long totalOrganizations = aggregations.stream()
                .mapToLong(PlanFeatureAdoptionProjection::getTotalOrganizations)
                .sum();

        long agendaEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getAgendaEnabledCount);
        long recomendacoesEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getRecomendacoesEnabledCount);
        long pagamentosEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getPagamentosEnabledCount);
        long suportePrioritarioEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getSuportePrioritarioEnabledCount);
        long monitoramentoEstoqueEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getMonitoramentoEstoqueEnabledCount);
        long metricasProdutoEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getMetricasProdutoEnabledCount);
        long integracaoMarketplaceEnabled = sumFeature(aggregations, PlanFeatureAdoptionProjection::getIntegracaoMarketplaceEnabledCount);

        return PlatformFeatureAdoptionDTO.builder()
                .totalOrganizations(totalOrganizations)
                .agenda(buildFeatureAdoptionMetrics(agendaEnabled, totalOrganizations))
                .recomendacoes(buildFeatureAdoptionMetrics(recomendacoesEnabled, totalOrganizations))
                .pagamentos(buildFeatureAdoptionMetrics(pagamentosEnabled, totalOrganizations))
                .suportePrioritario(buildFeatureAdoptionMetrics(suportePrioritarioEnabled, totalOrganizations))
                .monitoramentoEstoque(buildFeatureAdoptionMetrics(monitoramentoEstoqueEnabled, totalOrganizations))
                .metricasProduto(buildFeatureAdoptionMetrics(metricasProdutoEnabled, totalOrganizations))
                .integracaoMarketplace(buildFeatureAdoptionMetrics(integracaoMarketplaceEnabled, totalOrganizations))
                .build();
    }

    private InventoryAlertDTO toDto(InventoryAlert alert) {
        return InventoryAlertDTO.builder()
                .produtoId(alert.getProduto().getId())
                .nomeProduto(alert.getProduto().getNome())
                .severity(alert.getSeverity())
                .estoqueAtual(alert.getEstoqueAtual())
                .estoqueMinimo(alert.getEstoqueMinimo())
                .prazoReposicaoDias(alert.getPrazoReposicaoDias())
                .consumoMedioDiario(alert.getConsumoMedioDiario())
                .diasRestantes(alert.getDiasRestantes())
                .dataEstimadaRuptura(alert.getDataEstimadaRuptura())
                .quantidadeSugerida(alert.getQuantidadeSugerida())
                .mensagem(alert.getMensagem())
                .build();
    }

    private BigDecimal calcularProgresso(BigDecimal total, BigDecimal meta) {
        if (meta == null || BigDecimal.ZERO.compareTo(meta) == 0) {
            return null;
        }
        BigDecimal valorTotal = total != null ? total : BigDecimal.ZERO;
        return valorTotal.multiply(BigDecimal.valueOf(100))
                .divide(meta, 2, RoundingMode.HALF_UP);
    }

    private PlatformFeatureAdoptionDTO.FeatureAdoptionMetrics buildFeatureAdoptionMetrics(long enabledOrganizations,
                                                                                        long totalOrganizations) {
        BigDecimal adoptionPercentage;
        if (totalOrganizations == 0) {
            adoptionPercentage = BigDecimal.ZERO;
        } else {
            adoptionPercentage = BigDecimal.valueOf(enabledOrganizations)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOrganizations), 2, RoundingMode.HALF_UP);
        }

        return PlatformFeatureAdoptionDTO.FeatureAdoptionMetrics.builder()
                .organizations(enabledOrganizations)
                .adoptionPercentage(adoptionPercentage)
                .build();
    }

    private long sumFeature(List<PlanFeatureAdoptionProjection> aggregations,
                            ToLongFunction<PlanFeatureAdoptionProjection> extractor) {
        return aggregations.stream()
                .mapToLong(extractor)
                .sum();
    }

    private Organization getCurrentOrganizationOrThrow() {
        User user = CurrentUser.get();
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));
    }

    private BigDecimal calcularCrescimentoMensal(List<Object[]> totaisMensais,
                                                 YearMonth mesAtual,
                                                 YearMonth mesAnterior) {
        if (totaisMensais == null || totaisMensais.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<YearMonth, BigDecimal> totaisPorMes = new HashMap<>();
        for (Object[] linha : totaisMensais) {
            if (linha == null || linha.length < 3) {
                continue;
            }
            Integer ano = (Integer) linha[0];
            Integer mes = (Integer) linha[1];
            BigDecimal total = extractBigDecimal(linha[2]);
            if (ano != null && mes != null) {
                totaisPorMes.put(YearMonth.of(ano, mes), defaultZero(total));
            }
        }

        BigDecimal totalMesAtual = totaisPorMes.getOrDefault(mesAtual, BigDecimal.ZERO);
        BigDecimal totalMesAnterior = totaisPorMes.getOrDefault(mesAnterior, BigDecimal.ZERO);

        if (totalMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return totalMesAtual.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }

        return totalMesAtual.subtract(totalMesAnterior)
                .divide(totalMesAnterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Float valor) {
        return valor != null ? BigDecimal.valueOf(valor.doubleValue()) : BigDecimal.ZERO;
    }

    private BigDecimal calcularReceitaMensalRecorrente(BigDecimal valorPlano, Integer duracaoDias) {
        if (valorPlano == null || duracaoDias == null || duracaoDias <= 0) {
            return BigDecimal.ZERO;
        }

        return valorPlano.multiply(BigDecimal.valueOf(30))
                .divide(BigDecimal.valueOf(duracaoDias), 6, RoundingMode.HALF_UP);
    }

    private PlatformHealthScoreDTO.HealthSegment buildHealthSegment(long quantity, long total) {
        BigDecimal percentage = BigDecimal.ZERO;
        if (total > 0 && quantity > 0) {
            percentage = BigDecimal.valueOf(quantity)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }
        return PlatformHealthScoreDTO.HealthSegment.builder()
                .organizations(quantity)
                .percentage(percentage)
                .build();
    }

    private Set<Integer> sanitizeOrganizationIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(id))
                .collect(Collectors.toSet());
    }

    private BigDecimal extractBigDecimal(Object valor) {
        if (valor instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (valor instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calcularTaxaRetencao(long ativos, long totalBase) {
        if (totalBase <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(ativos)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalBase), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTempoMedioPrimeiraVenda(Map<Integer, LocalDate> primeirasVendas) {
        if (primeirasVendas == null || primeirasVendas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<Integer> organizationIds = primeirasVendas.keySet();
        Iterable<Organization> organizations = organizationRepository.findAllById(organizationIds);

        BigDecimal totalDias = BigDecimal.ZERO;
        long contador = 0L;

        for (Organization organization : organizations) {
            if (organization == null) {
                continue;
            }
            LocalDate dataAssinatura = organization.getDataAssinatura();
            LocalDate primeiraVenda = primeirasVendas.get(organization.getId());

            if (dataAssinatura == null || primeiraVenda == null) {
                continue;
            }

            long dias = ChronoUnit.DAYS.between(dataAssinatura, primeiraVenda);
            if (dias < 0) {
                dias = 0;
            }

            totalDias = totalDias.add(BigDecimal.valueOf(dias));
            contador++;
        }

        if (contador == 0L) {
            return BigDecimal.ZERO;
        }

        return totalDias.divide(BigDecimal.valueOf(contador), 2, RoundingMode.HALF_UP);
    }

    private boolean isPlatformOrganization(Integer organizationId) {
        return organizationId != null && PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(organizationId);
    }

    private boolean isPlatformOrganization(Organization organization) {
        return organization != null && isPlatformOrganization(organization.getId());
    }

    private Map<LocalDate, Long> toDailyCountMap(List<Object[]> linhas) {
        if (linhas == null || linhas.isEmpty()) {
            return Map.of();
        }

        Map<LocalDate, Long> resultado = new HashMap<>();
        for (Object[] linha : linhas) {
            if (linha == null || linha.length < 2) {
                continue;
            }
            LocalDate data = toLocalDate(linha[0]);
            Long quantidade = toLong(linha[1]);
            if (data != null && quantidade != null) {
                resultado.merge(data, quantidade, Long::sum);
            }
        }
        return resultado;
    }

    private List<PlatformOrganizationsResumoDTO.TimeSeriesPoint> buildTimeSeries(LocalDate inicio,
                                                                                LocalDate fim,
                                                                                Map<LocalDate, Long> valores) {
        List<PlatformOrganizationsResumoDTO.TimeSeriesPoint> pontos = new ArrayList<>();
        if (inicio == null || fim == null || valores == null) {
            return pontos;
        }

        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            long quantidade = valores.getOrDefault(cursor, 0L);
            pontos.add(PlatformOrganizationsResumoDTO.TimeSeriesPoint.builder()
                    .data(cursor)
                    .quantidade(quantidade)
                    .build());
            cursor = cursor.plusDays(1);
        }
        return pontos;
    }

    private LocalDate toLocalDate(Object valor) {
        if (valor instanceof LocalDate localDate) {
            return localDate;
        }
        if (valor instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (valor instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (valor instanceof java.util.Date utilDate) {
            return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private Long toLong(Object valor) {
        if (valor instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}

