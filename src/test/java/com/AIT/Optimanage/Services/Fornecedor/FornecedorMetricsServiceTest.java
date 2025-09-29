package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorMetricsServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private FornecedorMetricsService fornecedorMetricsService;

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1);
        fornecedor.setTenantId(10);
    }

    @Test
    void deveCalcularMetricasParaComprasConcretizadas() {
        Compra compra1 = criarCompra(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 4),
                LocalDateTime.of(2024, 1, 3, 12, 0), new BigDecimal("100.00"), StatusCompra.CONCRETIZADO);
        Compra compra2 = criarCompra(LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 6),
                LocalDateTime.of(2024, 1, 6, 12, 0), new BigDecimal("300.00"), StatusCompra.CONCRETIZADO);

        when(fornecedorRepository.findByIdAndOrganizationId(1, 10)).thenReturn(Optional.of(fornecedor));
        when(compraRepository.findByFornecedorIdAndOrganizationIdAndStatusIn(anyInt(), anyInt(), any()))
                .thenReturn(List.of(compra1, compra2));
        when(fornecedorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        fornecedorMetricsService.atualizarMetricasFornecedor(1, 10);

        assertThat(fornecedor.getLeadTimeMedioDias()).isEqualByComparingTo("2.00");
        assertThat(fornecedor.getTaxaEntregaNoPrazo()).isEqualByComparingTo("100.00");
        assertThat(fornecedor.getCustoMedioPedido()).isEqualByComparingTo("200.00");

        verify(fornecedorRepository).save(fornecedor);
    }

    @Test
    void deveConsiderarComprasPagasEAtrasos() {
        Compra compraNoPrazo = criarCompra(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 3),
                LocalDateTime.of(2024, 2, 2, 10, 0), new BigDecimal("150.00"), StatusCompra.PAGO);
        Compra compraAtrasada = criarCompra(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 2),
                LocalDateTime.of(2024, 2, 3, 10, 0), new BigDecimal("150.00"), StatusCompra.PAGO);

        when(fornecedorRepository.findByIdAndOrganizationId(1, 10)).thenReturn(Optional.of(fornecedor));
        when(compraRepository.findByFornecedorIdAndOrganizationIdAndStatusIn(anyInt(), anyInt(), any()))
                .thenReturn(List.of(compraNoPrazo, compraAtrasada));
        when(fornecedorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        fornecedorMetricsService.atualizarMetricasFornecedor(1, 10);

        assertThat(fornecedor.getLeadTimeMedioDias()).isEqualByComparingTo("1.92");
        assertThat(fornecedor.getTaxaEntregaNoPrazo()).isEqualByComparingTo("50.00");
        assertThat(fornecedor.getCustoMedioPedido()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveZerarMetricasQuandoNaoExistemComprasElegiveis() {
        fornecedor.setLeadTimeMedioDias(new BigDecimal("5.00"));
        fornecedor.setTaxaEntregaNoPrazo(new BigDecimal("80.00"));
        fornecedor.setCustoMedioPedido(new BigDecimal("250.00"));

        when(fornecedorRepository.findByIdAndOrganizationId(1, 10)).thenReturn(Optional.of(fornecedor));
        when(compraRepository.findByFornecedorIdAndOrganizationIdAndStatusIn(anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(fornecedorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        fornecedorMetricsService.atualizarMetricasFornecedor(1, 10);

        assertThat(fornecedor.getLeadTimeMedioDias()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fornecedor.getTaxaEntregaNoPrazo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fornecedor.getCustoMedioPedido()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Compra criarCompra(LocalDate dataEfetuacao, LocalDate dataAgendada, LocalDateTime dataConclusao,
                               BigDecimal valorFinal, StatusCompra status) {
        Compra compra = new Compra();
        compra.setFornecedor(fornecedor);
        compra.setTenantId(fornecedor.getOrganizationId());
        compra.setDataEfetuacao(dataEfetuacao);
        compra.setDataAgendada(dataAgendada);
        compra.setValorFinal(valorFinal);
        compra.setStatus(status);
        compra.setUpdatedAt(dataConclusao);
        return compra;
    }
}
