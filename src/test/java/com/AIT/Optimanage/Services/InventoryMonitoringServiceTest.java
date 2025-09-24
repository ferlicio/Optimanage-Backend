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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryMonitoringServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private InventoryHistoryRepository historyRepository;
    @Mock
    private InventoryAlertRepository alertRepository;
    @Mock
    private PlanoService planoService;

    private Clock clock;

    private InventoryMonitoringService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(LocalDate.of(2024, 1, 30).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        service = new InventoryMonitoringService(produtoRepository, historyRepository, alertRepository, planoService, clock);
    }

    @Test
    void deveGerarAlertaCriticoQuandoEstoqueAbaixoDoMinimo() {
        Produto produto = Produto.builder()
                .id(1)
                .qtdEstoque(5)
                .estoqueMinimo(10)
                .prazoReposicaoDias(3)
                .ativo(true)
                .build();
        produto.setTenantId(99);

        InventoryHistory h1 = InventoryHistory.builder()
                .quantidade(4)
                .build();
        h1.setCreatedAt(LocalDateTime.of(2024, 1, 28, 10, 0));
        InventoryHistory h2 = InventoryHistory.builder()
                .quantidade(3)
                .build();
        h2.setCreatedAt(LocalDateTime.of(2024, 1, 29, 10, 0));

        when(planoService.isMonitoramentoEstoqueHabilitado(99)).thenReturn(true);
        when(produtoRepository.findAllByOrganizationIdAndAtivoTrue(99)).thenReturn(List.of(produto));
        when(historyRepository.findByProdutoIdAndOrganizationIdAndActionAndCreatedAtAfterOrderByCreatedAtAsc(
                eq(1), eq(99), eq(InventoryAction.DECREMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(h1, h2));
        when(alertRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<InventoryAlert> alertas = service.recalcularAlertasOrganizacao(99);

        assertThat(alertas).hasSize(1);
        InventoryAlert alerta = alertas.get(0);
        assertThat(alerta.getSeverity()).isEqualTo(InventoryAlertSeverity.CRITICAL);
        assertThat(alerta.getQuantidadeSugerida()).isGreaterThan(0);
        assertThat(alerta.getConsumoMedioDiario()).isGreaterThan(BigDecimal.ZERO);

        verify(alertRepository).deleteByOrganizationId(99);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InventoryAlert>> captor = ArgumentCaptor.forClass(List.class);
        verify(alertRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getProduto()).isEqualTo(produto);
    }

    @Test
    void naoDeveGerarAlertasQuandoEstoqueSaudavel() {
        Produto produto = Produto.builder()
                .id(2)
                .qtdEstoque(50)
                .estoqueMinimo(10)
                .prazoReposicaoDias(5)
                .ativo(true)
                .build();
        produto.setTenantId(55);

        InventoryHistory consumo = InventoryHistory.builder()
                .quantidade(2)
                .build();
        consumo.setCreatedAt(LocalDateTime.of(2024, 1, 25, 9, 0));

        when(planoService.isMonitoramentoEstoqueHabilitado(55)).thenReturn(true);
        when(produtoRepository.findAllByOrganizationIdAndAtivoTrue(55)).thenReturn(List.of(produto));
        when(historyRepository.findByProdutoIdAndOrganizationIdAndActionAndCreatedAtAfterOrderByCreatedAtAsc(
                eq(2), eq(55), eq(InventoryAction.DECREMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(consumo));

        List<InventoryAlert> alertas = service.recalcularAlertasOrganizacao(55);

        assertThat(alertas).isEmpty();
        verify(alertRepository).deleteByOrganizationId(55);
        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void deveLimparAlertasQuandoPlanoNaoPermiteMonitoramento() {
        when(planoService.isMonitoramentoEstoqueHabilitado(77)).thenReturn(false);

        List<InventoryAlert> alertas = service.recalcularAlertasOrganizacao(77);

        assertThat(alertas).isEmpty();
        verify(alertRepository).deleteByOrganizationId(77);
        verifyNoInteractions(produtoRepository);
        verifyNoInteractions(historyRepository);
        verify(alertRepository, never()).saveAll(anyList());
    }
}
