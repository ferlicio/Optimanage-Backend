package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.InventoryMonitoringService;
import com.AIT.Optimanage.Services.PlanoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(vendaRepository, compraRepository, inventoryMonitoringService, planoService);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void shouldCalculateResumoUsingRepositoryAggregates() {
        int organizationId = 1;
        CurrentUser.set(buildUser(organizationId));

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
        CurrentUser.set(buildUser(organizationId));

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

        CurrentUser.set(buildUser(firstOrganization));
        ResumoDTO firstResumo = analyticsService.obterResumo();
        assertBigDecimalEquals(BigDecimal.valueOf(75), firstResumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(25), firstResumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(50), firstResumo.getLucro());
        verify(vendaRepository).sumValorFinalByOrganization(firstOrganization);
        verify(compraRepository).sumValorFinalByOrganization(firstOrganization);

        CurrentUser.set(buildUser(secondOrganization));
        ResumoDTO secondResumo = analyticsService.obterResumo();
        assertBigDecimalEquals(BigDecimal.valueOf(120), secondResumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(30), secondResumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(90), secondResumo.getLucro());
        verify(vendaRepository).sumValorFinalByOrganization(secondOrganization);
        verify(compraRepository).sumValorFinalByOrganization(secondOrganization);
    }

    private User buildUser(int organizationId) {
        User user = User.builder()
                .nome("Test")
                .sobrenome("User")
                .email("test@example.com")
                .senha("secret")
                .role(Role.ADMIN)
                .build();
        user.setTenantId(organizationId);
        return user;
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), () -> "Expected " + expected + " but was " + actual);
    }
}
