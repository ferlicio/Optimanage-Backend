package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.InventoryMonitoringService;
import com.AIT.Optimanage.Services.PlanoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private CompraRepository compraRepository;
    @Mock
    private InventoryMonitoringService inventoryMonitoringService;
    @Mock
    private PlanoService planoService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User usuarioAtual;

    @BeforeEach
    void setUp() {
        usuarioAtual = buildUser(42);
        CurrentUser.set(usuarioAtual);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void shouldCalculateResumoUsingRepositoryAggregates() {
        int organizationId = 1;
        usuarioAtual = buildUser(organizationId);
        CurrentUser.set(usuarioAtual);

        when(vendaRepository.sumValorFinalByOrganization(organizationId)).thenReturn(BigDecimal.valueOf(150));
        when(compraRepository.sumValorFinalByOrganization(organizationId)).thenReturn(BigDecimal.valueOf(90));

        ResumoDTO resumo = analyticsService.obterResumo();

        assertNotNull(resumo);
        assertBigDecimalEquals(BigDecimal.valueOf(150), resumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(90), resumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(60), resumo.getLucro());
    }

    @Test
    void shouldTreatNullAggregatesAsZero() {
        int organizationId = 5;
        usuarioAtual = buildUser(organizationId);
        CurrentUser.set(usuarioAtual);

        when(vendaRepository.sumValorFinalByOrganization(organizationId)).thenReturn(null);
        when(compraRepository.sumValorFinalByOrganization(organizationId)).thenReturn(null);

        ResumoDTO resumo = analyticsService.obterResumo();

        assertNotNull(resumo);
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getLucro());
    }

    @Test
    void shouldIsolateResumoByOrganization() {
        int firstOrganization = 10;
        int secondOrganization = 20;

        when(vendaRepository.sumValorFinalByOrganization(firstOrganization)).thenReturn(BigDecimal.valueOf(75));
        when(compraRepository.sumValorFinalByOrganization(firstOrganization)).thenReturn(BigDecimal.valueOf(25));
        when(vendaRepository.sumValorFinalByOrganization(secondOrganization)).thenReturn(BigDecimal.valueOf(120));
        when(compraRepository.sumValorFinalByOrganization(secondOrganization)).thenReturn(BigDecimal.valueOf(30));

        usuarioAtual = buildUser(firstOrganization);
        CurrentUser.set(usuarioAtual);
        ResumoDTO firstResumo = analyticsService.obterResumo();
        assertBigDecimalEquals(BigDecimal.valueOf(75), firstResumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(25), firstResumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(50), firstResumo.getLucro());
        verify(vendaRepository).sumValorFinalByOrganization(firstOrganization);
        verify(compraRepository).sumValorFinalByOrganization(firstOrganization);

        usuarioAtual = buildUser(secondOrganization);
        CurrentUser.set(usuarioAtual);
        ResumoDTO secondResumo = analyticsService.obterResumo();
        assertBigDecimalEquals(BigDecimal.valueOf(120), secondResumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(30), secondResumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(90), secondResumo.getLucro());
        verify(vendaRepository).sumValorFinalByOrganization(secondOrganization);
        verify(compraRepository).sumValorFinalByOrganization(secondOrganization);
    }

    @Test
    void preverDemandaAgregaVendasDoMesmoDia() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);

        List<Venda> vendasComDuplicidade = List.of(
                criarVenda(inicio, BigDecimal.valueOf(100)),
                criarVenda(inicio, BigDecimal.valueOf(50)),
                criarVenda(inicio.plusDays(1), BigDecimal.valueOf(200))
        );

        List<Venda> vendasAgregadas = List.of(
                criarVenda(inicio, BigDecimal.valueOf(150)),
                criarVenda(inicio.plusDays(1), BigDecimal.valueOf(200))
        );

        when(vendaRepository.findAll()).thenReturn(vendasComDuplicidade).thenReturn(vendasAgregadas);

        PrevisaoDTO previsaoDuplicados = analyticsService.preverDemanda();
        PrevisaoDTO previsaoAgregada = analyticsService.preverDemanda();

        assertThat(previsaoDuplicados.getValorPrevisto().doubleValue())
                .isCloseTo(previsaoAgregada.getValorPrevisto().doubleValue(), within(1e-6));
    }

    @Test
    void previsaoConsideraEspacamentoTemporalIrregular() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);

        List<Venda> vendasConsecutivas = List.of(
                criarVenda(inicio, BigDecimal.valueOf(10)),
                criarVenda(inicio.plusDays(1), BigDecimal.valueOf(20)),
                criarVenda(inicio.plusDays(2), BigDecimal.valueOf(30))
        );

        when(vendaRepository.findAll()).thenReturn(vendasConsecutivas);
        double previsaoConsecutiva = analyticsService.preverDemanda().getValorPrevisto().doubleValue();

        List<Venda> vendasEspacadas = List.of(
                criarVenda(inicio, BigDecimal.valueOf(10)),
                criarVenda(inicio.plusDays(10), BigDecimal.valueOf(20)),
                criarVenda(inicio.plusDays(20), BigDecimal.valueOf(30))
        );

        when(vendaRepository.findAll()).thenReturn(vendasEspacadas);
        double previsaoEspacada = analyticsService.preverDemanda().getValorPrevisto().doubleValue();

        assertThat(previsaoConsecutiva).isGreaterThan(previsaoEspacada);
    }

    private Venda criarVenda(LocalDate data, BigDecimal valorFinal) {
        Venda venda = Venda.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(data)
                .dataCobranca(data)
                .valorTotal(valorFinal)
                .descontoGeral(BigDecimal.ZERO)
                .valorFinal(valorFinal)
                .valorPendente(BigDecimal.ZERO)
                .build();
        venda.setTenantId(usuarioAtual.getTenantId());
        return venda;
    }

    private User buildUser(int organizationId) {
        User user = User.builder()
                .nome("Usuário")
                .sobrenome("Teste")
                .email("teste@example.com")
                .senha("senha-segura")
                .role(Role.ADMIN)
                .build();
        user.setTenantId(organizationId);
        return user;
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), () -> "Expected " + expected + " but was " + actual);
    }
}
