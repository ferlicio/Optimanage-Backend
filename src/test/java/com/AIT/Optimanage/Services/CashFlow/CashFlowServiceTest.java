package com.AIT.Optimanage.Services.CashFlow;

import com.AIT.Optimanage.Mappers.CashFlowMapper;
import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryResponse;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowOrigin;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import com.AIT.Optimanage.Models.CashFlow.Search.CashFlowSearch;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowEntryRepository;
import com.AIT.Optimanage.Repositories.Compra.PagamentoCompraRepository;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashFlowServiceTest {

    @Mock
    private CashFlowEntryRepository cashFlowEntryRepository;

    @Mock
    private PagamentoVendaRepository pagamentoVendaRepository;

    @Mock
    private PagamentoCompraRepository pagamentoCompraRepository;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    private CashFlowService cashFlowService;

    @BeforeEach
    void setUp() {
        CashFlowMapper mapper = Mappers.getMapper(CashFlowMapper.class);
        cashFlowService = new CashFlowService(cashFlowEntryRepository, pagamentoVendaRepository,
                pagamentoCompraRepository, mapper, planoAccessGuard);

        var user = com.AIT.Optimanage.Models.User.User.builder().build();
        user.setTenantId(99);
        CurrentUser.set(user);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void listarLancamentosCombinaLancamentosManuaisEParcelas() {
        CashFlowEntry manual = new CashFlowEntry();
        manual.setId(1);
        manual.setDescription("Mensalidade");
        manual.setAmount(BigDecimal.valueOf(100));
        manual.setType(CashFlowType.INCOME);
        manual.setMovementDate(LocalDate.now().minusDays(2));
        manual.setStatus(CashFlowStatus.ACTIVE);

        Page<CashFlowEntry> manualPage = new PageImpl<>(List.of(manual), PageRequest.of(0, 20), 1);
        when(cashFlowEntryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(manualPage);

        Venda venda = Venda.builder().sequencialUsuario(12).build();
        venda.setId(55);
        VendaPagamento parcelaVendaFutura = VendaPagamento.builder()
                .venda(venda)
                .valorPago(BigDecimal.valueOf(250))
                .statusPagamento(StatusPagamento.PENDENTE)
                .dataVencimento(LocalDate.now().plusDays(3))
                .build();
        parcelaVendaFutura.setId(200);

        VendaPagamento parcelaVendaCancelada = VendaPagamento.builder()
                .venda(venda)
                .valorPago(BigDecimal.valueOf(400))
                .statusPagamento(StatusPagamento.ESTORNADO)
                .dataVencimento(LocalDate.now().plusDays(1))
                .build();
        parcelaVendaCancelada.setId(201);

        when(pagamentoVendaRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parcelaVendaFutura, parcelaVendaCancelada)));

        Compra compra = Compra.builder().sequencialUsuario(33).build();
        compra.setId(88);
        compra.setFornecedor(Fornecedor.builder().nome("Fornecedor ABC").build());
        CompraPagamento parcelaCompraPaga = CompraPagamento.builder()
                .compra(compra)
                .valorPago(BigDecimal.valueOf(150))
                .statusPagamento(StatusPagamento.PAGO)
                .dataPagamento(LocalDate.now().minusDays(1))
                .dataVencimento(LocalDate.now().minusDays(2))
                .build();
        parcelaCompraPaga.setId(300);

        when(pagamentoCompraRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parcelaCompraPaga)));

        CashFlowSearch search = CashFlowSearch.builder().page(0).pageSize(10).build();

        Page<CashFlowEntryResponse> resultado = cashFlowService.listarLancamentos(search);

        assertThat(resultado.getTotalElements()).isEqualTo(3);
        assertThat(resultado.getContent()).hasSize(3);

        CashFlowEntryResponse manualResponse = resultado.getContent().get(0);
        CashFlowEntryResponse compraResponse = resultado.getContent().get(1);
        CashFlowEntryResponse vendaResponse = resultado.getContent().get(2);

        assertThat(manualResponse.getOrigin()).isEqualTo(CashFlowOrigin.MANUAL);
        assertThat(compraResponse.getOrigin()).isEqualTo(CashFlowOrigin.PURCHASE_INSTALLMENT);
        assertThat(compraResponse.getStatus()).isEqualTo(CashFlowStatus.ACTIVE);
        assertThat(vendaResponse.getOrigin()).isEqualTo(CashFlowOrigin.SALE_INSTALLMENT);
        assertThat(vendaResponse.getStatus()).isEqualTo(CashFlowStatus.SCHEDULED);

        assertThat(resultado.getContent())
                .extracting(CashFlowEntryResponse::getStatus)
                .doesNotContain(CashFlowStatus.CANCELLED);
    }

    @Test
    void filtrarPorStatusMantemSomenteParcelasComStatusCorrespondente() {
        Page<CashFlowEntry> manualPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(cashFlowEntryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(manualPage);

        Venda venda = Venda.builder().sequencialUsuario(10).build();
        venda.setId(501);
        VendaPagamento parcelaAgendada = VendaPagamento.builder()
                .venda(venda)
                .valorPago(BigDecimal.valueOf(500))
                .statusPagamento(StatusPagamento.PENDENTE)
                .dataVencimento(LocalDate.now().plusDays(5))
                .build();
        parcelaAgendada.setId(700);

        when(pagamentoVendaRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parcelaAgendada)));

        when(pagamentoCompraRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        CashFlowSearch search = CashFlowSearch.builder()
                .status(CashFlowStatus.SCHEDULED)
                .page(0)
                .pageSize(10)
                .build();

        Page<CashFlowEntryResponse> resultado = cashFlowService.listarLancamentos(search);

        assertThat(resultado.getContent()).hasSize(1);
        CashFlowEntryResponse unico = resultado.getContent().get(0);
        assertThat(unico.getOrigin()).isEqualTo(CashFlowOrigin.SALE_INSTALLMENT);
        assertThat(unico.getStatus()).isEqualTo(CashFlowStatus.SCHEDULED);
    }

    @Test
    void filtrarPorCanceladosConsultaApenasParcelasEstornadas() {
        when(cashFlowEntryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Venda venda = Venda.builder().sequencialUsuario(99).build();
        venda.setId(321);

        VendaPagamento parcelaCancelada = VendaPagamento.builder()
                .venda(venda)
                .valorPago(BigDecimal.valueOf(100))
                .statusPagamento(StatusPagamento.ESTORNADO)
                .dataVencimento(LocalDate.now().minusDays(1))
                .build();
        parcelaCancelada.setId(123);

        when(pagamentoVendaRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parcelaCancelada)));
        when(pagamentoCompraRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(any(), any(), any(), any(), anyBoolean(), anyString(), nullable(CashFlowStatus.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        CashFlowSearch search = CashFlowSearch.builder()
                .status(CashFlowStatus.CANCELLED)
                .page(0)
                .pageSize(10)
                .build();

        Page<CashFlowEntryResponse> resultado = cashFlowService.listarLancamentos(search);

        assertThat(resultado.getContent()).hasSize(1);
        CashFlowEntryResponse unico = resultado.getContent().get(0);
        assertThat(unico.getStatus()).isEqualTo(CashFlowStatus.CANCELLED);

        verify(pagamentoVendaRepository).findInstallmentsByOrganizationAndStatusesAndDateRange(any(),
                eq(List.of(StatusPagamento.ESTORNADO)), any(), any(), eq(true), eq("MOVEMENT_DATE"),
                eq(CashFlowStatus.CANCELLED), any(LocalDate.class), any(Pageable.class));
    }
}
