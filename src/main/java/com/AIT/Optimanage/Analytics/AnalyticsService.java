package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.InventoryAlertDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformResumoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
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
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (!PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(organization.getId())) {
            throw new AccessDeniedException("Acesso restrito à organização da plataforma");
        }
        return organization;
    }

    public PlatformResumoDTO obterResumoPlataforma() {
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

        return new PlatformResumoDTO(totalVendas, totalCompras, lucro, ticketMedio, crescimentoMensal);
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

    private BigDecimal extractBigDecimal(Object valor) {
        if (valor instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (valor instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}

