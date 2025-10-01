package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.InventoryAlertDTO;
import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Services.InventoryMonitoringService;
import com.AIT.Optimanage.Services.PlanoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final CompraRepository compraRepository;
    private final InventoryMonitoringService inventoryMonitoringService;
    private final PlanoService planoService;

    public ResumoDTO obterResumo() {
        User user = CurrentUser.get();
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
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

        BigDecimal lucro = totalVendas.subtract(totalCompras);
        return new ResumoDTO(totalVendas, totalCompras, lucro);
    }

    public PrevisaoDTO preverDemanda() {
        User user = CurrentUser.get();
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        List<Venda> vendas = vendaRepository.findAll().stream()
                .filter(v -> organizationId.equals(v.getOrganizationId()))
                .sorted(Comparator.comparing(Venda::getDataEfetuacao))
                .toList();

        if (vendas.size() < 2) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        List<DailySalesPoint> pontosDiarios = agregarPorDia(vendas);
        if (pontosDiarios.size() < 2) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        long proximoDiaIndex = pontosDiarios.get(pontosDiarios.size() - 1).dayIndex() + 1;
        double forecast = preverComPesosRecencia(pontosDiarios, proximoDiaIndex);

        double ajusteSazonal = calcularAjusteSazonal(pontosDiarios);
        double previsaoAjustada = forecast;
        if (!Double.isNaN(ajusteSazonal)) {
            previsaoAjustada = (forecast * 0.7) + (ajusteSazonal * 0.3);
        }

        return new PrevisaoDTO(BigDecimal.valueOf(previsaoAjustada));
    }

    private List<DailySalesPoint> agregarPorDia(List<Venda> vendas) {
        Map<LocalDate, BigDecimal> totalPorDia = new LinkedHashMap<>();
        for (Venda venda : vendas) {
            LocalDate data = venda.getDataEfetuacao();
            if (data == null) {
                continue;
            }
            BigDecimal valor = venda.getValorFinal();
            if (valor == null) {
                valor = BigDecimal.ZERO;
            }
            totalPorDia.merge(data, valor, BigDecimal::add);
        }

        if (totalPorDia.isEmpty()) {
            return List.of();
        }

        LocalDate primeiraData = totalPorDia.keySet().iterator().next();
        List<DailySalesPoint> pontos = new ArrayList<>(totalPorDia.size());
        for (Map.Entry<LocalDate, BigDecimal> entry : totalPorDia.entrySet()) {
            long dayIndex = ChronoUnit.DAYS.between(primeiraData, entry.getKey());
            pontos.add(new DailySalesPoint(entry.getKey(), entry.getValue().doubleValue(), dayIndex));
        }
        return pontos;
    }

    private double preverComPesosRecencia(List<DailySalesPoint> pontosDiarios, long proximoDiaIndex) {
        int n = pontosDiarios.size();
        double sumW = 0;
        double sumWX = 0;
        double sumWY = 0;
        double sumWXX = 0;
        double sumWXY = 0;

        int ultimoIndice = n - 1;
        for (int i = 0; i < n; i++) {
            DailySalesPoint ponto = pontosDiarios.get(i);
            double x = ponto.dayIndex();
            double y = ponto.total();
            double pesoRecencia = 1.0 + ((double) i / Math.max(1, ultimoIndice));
            sumW += pesoRecencia;
            sumWX += pesoRecencia * x;
            sumWY += pesoRecencia * y;
            sumWXX += pesoRecencia * x * x;
            sumWXY += pesoRecencia * x * y;
        }

        double denominador = (sumW * sumWXX) - (sumWX * sumWX);
        if (Math.abs(denominador) < 1e-9) {
            return pontosDiarios.get(ultimoIndice).total();
        }

        double slope = ((sumW * sumWXY) - (sumWX * sumWY)) / denominador;
        double intercept = (sumWY - (slope * sumWX)) / sumW;
        return intercept + (slope * proximoDiaIndex);
    }

    private double calcularAjusteSazonal(List<DailySalesPoint> pontosDiarios) {
        DailySalesPoint ultimoPonto = pontosDiarios.get(pontosDiarios.size() - 1);
        LocalDate proximaData = ultimoPonto.date().plusDays(1);
        int mes = proximaData.getMonthValue();

        OptionalDouble mediaMensal = pontosDiarios.stream()
                .filter(p -> p.date().getMonthValue() == mes)
                .mapToDouble(DailySalesPoint::total)
                .average();

        return mediaMensal.orElse(Double.NaN);
    }

    private record DailySalesPoint(LocalDate date, double total, long dayIndex) {
    }

    public List<InventoryAlertDTO> listarAlertasEstoque() {
        User user = CurrentUser.get();
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
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
}

