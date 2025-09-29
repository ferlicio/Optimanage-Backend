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
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
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

        // Forecast using a simple linear regression from Apache Commons Math.
        // This placeholder approach can be replaced by an AI-based model in the future.
        SimpleRegression regression = new SimpleRegression();
        int i = 0;
        for (Venda venda : vendas) {
            regression.addData(i++, venda.getValorFinal().doubleValue());
        }

        double forecast = regression.predict(i);
        return new PrevisaoDTO(BigDecimal.valueOf(forecast));
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

