package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.PlatformOnboardingMetricsDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformOrganizationsResumoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.TrialType;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationOnboardingProjection;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.InventoryMonitoringService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Support.PlatformConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private OrganizationRepository organizationRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(vendaRepository, compraRepository, inventoryMonitoringService, planoService, organizationRepository);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void shouldCalculateResumoUsingRepositoryAggregatesAndGoals() {
        int organizationId = 1;
        CurrentUser.set(buildUser(organizationId));
        Organization organization = buildOrganization(organizationId, 100f, 1200f);

        when(organizationRepository.findById(organizationId)).thenReturn(java.util.Optional.of(organization));
        when(vendaRepository.sumValorFinalByOrganization(organizationId)).thenReturn(BigDecimal.valueOf(150));
        when(compraRepository.sumValorFinalByOrganization(organizationId)).thenReturn(BigDecimal.valueOf(90));
        when(vendaRepository.sumValorFinalByOrganizationBetweenDates(eq(organizationId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(50), BigDecimal.valueOf(600));

        ResumoDTO resumo = analyticsService.obterResumo();

        assertNotNull(resumo);
        assertBigDecimalEquals(BigDecimal.valueOf(150), resumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.valueOf(90), resumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(60), resumo.getLucro());
        assertBigDecimalEquals(BigDecimal.valueOf(100), resumo.getMetaMensal());
        assertBigDecimalEquals(BigDecimal.valueOf(1200), resumo.getMetaAnual());
        assertBigDecimalEquals(BigDecimal.valueOf(50), resumo.getProgressoMensal());
        assertBigDecimalEquals(BigDecimal.valueOf(50), resumo.getProgressoAnual());
    }

    @Test
    void shouldTreatNullAggregatesAsZeroWhenGoalsMissing() {
        int organizationId = 5;
        CurrentUser.set(buildUser(organizationId));
        Organization organization = buildOrganization(organizationId, null, null);

        when(organizationRepository.findById(organizationId)).thenReturn(java.util.Optional.of(organization));
        when(vendaRepository.sumValorFinalByOrganization(organizationId)).thenReturn(null);
        when(compraRepository.sumValorFinalByOrganization(organizationId)).thenReturn(null);
        when(vendaRepository.sumValorFinalByOrganizationBetweenDates(eq(organizationId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        ResumoDTO resumo = analyticsService.obterResumo();

        assertNotNull(resumo);
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getTotalVendas());
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getTotalCompras());
        assertBigDecimalEquals(BigDecimal.ZERO, resumo.getLucro());
        assertNull(resumo.getMetaMensal());
        assertNull(resumo.getMetaAnual());
        assertNull(resumo.getProgressoMensal());
        assertNull(resumo.getProgressoAnual());
    }

    @Test
    void shouldIsolateResumoByOrganization() {
        int firstOrganization = 10;
        int secondOrganization = 20;
        Organization first = buildOrganization(firstOrganization, 50f, 500f);
        Organization second = buildOrganization(secondOrganization, 80f, 800f);

        when(organizationRepository.findById(firstOrganization)).thenReturn(java.util.Optional.of(first));
        when(organizationRepository.findById(secondOrganization)).thenReturn(java.util.Optional.of(second));
        when(vendaRepository.sumValorFinalByOrganization(firstOrganization)).thenReturn(BigDecimal.valueOf(75));
        when(compraRepository.sumValorFinalByOrganization(firstOrganization)).thenReturn(BigDecimal.valueOf(25));
        when(vendaRepository.sumValorFinalByOrganization(secondOrganization)).thenReturn(BigDecimal.valueOf(120));
        when(compraRepository.sumValorFinalByOrganization(secondOrganization)).thenReturn(BigDecimal.valueOf(30));
        when(vendaRepository.sumValorFinalByOrganizationBetweenDates(eq(firstOrganization), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(vendaRepository.sumValorFinalByOrganizationBetweenDates(eq(secondOrganization), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

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

    @Test
    void shouldAggregateDailySalesBeforeForecasting() {
        int organizationId = 30;
        CurrentUser.set(buildUser(organizationId));

        when(organizationRepository.findById(organizationId)).thenReturn(java.util.Optional.of(buildOrganization(organizationId, null, null)));

        List<Venda> duplicatedSales = List.of(
                venda(organizationId, LocalDate.of(2024, 1, 1), new BigDecimal("100")),
                venda(organizationId, LocalDate.of(2024, 1, 1), new BigDecimal("50")),
                venda(organizationId, LocalDate.of(2024, 1, 11), new BigDecimal("200")),
                venda(organizationId, LocalDate.of(2024, 2, 10), new BigDecimal("300"))
        );

        List<Venda> aggregatedSales = List.of(
                venda(organizationId, LocalDate.of(2024, 1, 1), new BigDecimal("150")),
                venda(organizationId, LocalDate.of(2024, 1, 11), new BigDecimal("200")),
                venda(organizationId, LocalDate.of(2024, 2, 10), new BigDecimal("300"))
        );

        when(vendaRepository.findAll()).thenReturn(duplicatedSales, aggregatedSales);

        PrevisaoDTO forecastWithDuplicates = analyticsService.preverDemanda();
        PrevisaoDTO forecastAggregated = analyticsService.preverDemanda();

        assertNotNull(forecastWithDuplicates);
        assertNotNull(forecastAggregated);
        assertBigDecimalEquals(
                forecastAggregated.getValorPrevisto(),
                forecastWithDuplicates.getValorPrevisto()
        );
    }

    @Test
    void shouldReactToIrregularIntervalsInForecast() {
        int organizationId = 40;
        CurrentUser.set(buildUser(organizationId));

        when(organizationRepository.findById(organizationId)).thenReturn(java.util.Optional.of(buildOrganization(organizationId, null, null)));

        List<Venda> irregularSpacing = List.of(
                venda(organizationId, LocalDate.of(2024, 1, 1), new BigDecimal("150")),
                venda(organizationId, LocalDate.of(2024, 1, 11), new BigDecimal("200")),
                venda(organizationId, LocalDate.of(2024, 2, 10), new BigDecimal("300"))
        );

        List<Venda> uniformSpacing = List.of(
                venda(organizationId, LocalDate.of(2024, 1, 1), new BigDecimal("150")),
                venda(organizationId, LocalDate.of(2024, 1, 2), new BigDecimal("200")),
                venda(organizationId, LocalDate.of(2024, 1, 3), new BigDecimal("300"))
        );

        when(vendaRepository.findAll()).thenReturn(irregularSpacing, uniformSpacing);

        PrevisaoDTO irregularForecast = analyticsService.preverDemanda();
        PrevisaoDTO uniformForecast = analyticsService.preverDemanda();

        assertNotNull(irregularForecast);
        assertNotNull(uniformForecast);
        assertNotEquals(0, irregularForecast.getValorPrevisto().compareTo(uniformForecast.getValorPrevisto()));
        assertTrue(irregularForecast.getValorPrevisto().compareTo(uniformForecast.getValorPrevisto()) < 0,
                () -> "Expected irregular interval forecast to be lower due to slower growth, but was "
                        + irregularForecast.getValorPrevisto().setScale(3, RoundingMode.HALF_UP)
                        + " vs "
                        + uniformForecast.getValorPrevisto().setScale(3, RoundingMode.HALF_UP));
    }

    @Test
    void shouldSummarizePlatformOrganizationsResumo() {
        CurrentUser.set(buildUser(PlatformConstants.PLATFORM_ORGANIZATION_ID));

        Organization platformOrganization = buildOrganization(PlatformConstants.PLATFORM_ORGANIZATION_ID, null, null);
        when(organizationRepository.findById(PlatformConstants.PLATFORM_ORGANIZATION_ID))
                .thenReturn(java.util.Optional.of(platformOrganization));

        LocalDate today = LocalDate.now();
        LocalDate createdDate = today.minusDays(5);
        LocalDate signedDate = today.minusDays(2);

        List<Object[]> criadasAggregate = List.<Object[]>of(new Object[]{createdDate, 2L});
        List<Object[]> assinadasAggregate = List.<Object[]>of(new Object[]{signedDate, 1L});

        when(organizationRepository.countOrganizationsCreatedByDateRange(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(PlatformConstants.PLATFORM_ORGANIZATION_ID)
        )).thenReturn(criadasAggregate);

        when(organizationRepository.countOrganizationsSignedByDateRange(
                any(LocalDate.class),
                any(LocalDate.class),
                eq(PlatformConstants.PLATFORM_ORGANIZATION_ID)
        )).thenReturn(assinadasAggregate);

        when(organizationRepository.countAllExcluding(PlatformConstants.PLATFORM_ORGANIZATION_ID)).thenReturn(5L);
        when(organizationRepository.countOrganizationsActiveByDateRange(any(LocalDate.class), any(LocalDate.class),
                eq(PlatformConstants.PLATFORM_ORGANIZATION_ID))).thenReturn(3L);

        PlatformOrganizationsResumoDTO overview = analyticsService.obterResumoPlataforma();

        assertNotNull(overview);
        assertEquals(3, overview.getTotalAtivas());
        assertEquals(2, overview.getTotalInativas());
        assertEquals(30, overview.getCriadas().size());
        assertEquals(30, overview.getAtivadas().size());

        verify(organizationRepository).countOrganizationsActiveByDateRange(
                any(LocalDate.class),
                any(LocalDate.class),
                eq(PlatformConstants.PLATFORM_ORGANIZATION_ID)
        );

        PlatformOrganizationsResumoDTO.TimeSeriesPoint createdPoint = findPointByDate(overview.getCriadas(), createdDate);
        assertNotNull(createdPoint);
        assertEquals(2L, createdPoint.getQuantidade());

        PlatformOrganizationsResumoDTO.TimeSeriesPoint activatedPoint = findPointByDate(overview.getAtivadas(), signedDate);
        assertNotNull(activatedPoint);
        assertEquals(1L, activatedPoint.getQuantidade());
    }

    @Test
    void shouldComputePlatformOnboardingMetrics() {
        CurrentUser.set(buildUser(PlatformConstants.PLATFORM_ORGANIZATION_ID));

        Organization platformOrganization = buildOrganization(PlatformConstants.PLATFORM_ORGANIZATION_ID, null, null);
        when(organizationRepository.findById(PlatformConstants.PLATFORM_ORGANIZATION_ID))
                .thenReturn(java.util.Optional.of(platformOrganization));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<OrganizationOnboardingProjection> allOrganizations = List.of(
                onboarding(
                        now.minusDays(40),
                        today.minusDays(35),
                        today.minusDays(40),
                        today.minusDays(26),
                        TrialType.PLAN_DEFAULT
                ),
                onboarding(
                        now.minusDays(20),
                        null,
                        today.minusDays(20),
                        today.minusDays(1),
                        TrialType.PLAN_DEFAULT
                ),
                onboarding(
                        now.minusDays(10),
                        today.minusDays(2),
                        today.minusDays(10),
                        today.minusDays(3),
                        TrialType.CUSTOM
                ),
                onboarding(
                        now.minusDays(5),
                        null,
                        today.minusDays(5),
                        today.plusDays(5),
                        TrialType.CUSTOM
                )
        );

        List<OrganizationOnboardingProjection> recentOrganizations = List.of(
                onboarding(
                        now.minusDays(20),
                        null,
                        today.minusDays(20),
                        today.minusDays(1),
                        TrialType.PLAN_DEFAULT
                ),
                onboarding(
                        now.minusDays(10),
                        today.minusDays(2),
                        today.minusDays(10),
                        today.minusDays(3),
                        TrialType.CUSTOM
                ),
                onboarding(
                        now.minusDays(5),
                        null,
                        today.minusDays(5),
                        today.plusDays(5),
                        TrialType.CUSTOM
                )
        );

        when(organizationRepository.findOrganizationOnboardingDates(any(), any(), any()))
                .thenReturn(allOrganizations, recentOrganizations);

        PlatformOnboardingMetricsDTO metrics = analyticsService.obterOnboardingMetricsPlataforma();

        assertNotNull(metrics);
        assertEquals(4, metrics.getTotalOrganizacoes());
        assertEquals(2, metrics.getTotalOrganizacoesAssinadas());
        assertBigDecimalEquals(new BigDecimal("6.50"), metrics.getTempoMedioDiasAteAssinatura());
        assertBigDecimalEquals(new BigDecimal("50.00"), metrics.getPercentualAssinatura7Dias());
        assertBigDecimalEquals(new BigDecimal("100.00"), metrics.getPercentualAssinatura30Dias());
        assertBigDecimalEquals(new BigDecimal("50.00"), metrics.getTaxaConversaoTotal());
        assertBigDecimalEquals(new BigDecimal("33.33"), metrics.getTaxaConversaoUltimos30Dias());
        assertEquals(4, metrics.getTotalTrials());
        assertEquals(1, metrics.getTrialsAtivos());
        assertEquals(1, metrics.getTrialsExpirados());
        assertBigDecimalEquals(new BigDecimal("50.00"), metrics.getTaxaConversaoTrials());
        assertBigDecimalEquals(new BigDecimal("25.00"), metrics.getTaxaConversaoTrialsNoPrazo());
    }

    @Test
    void shouldReturnZeroedOnboardingMetricsWhenNoOrganizations() {
        CurrentUser.set(buildUser(PlatformConstants.PLATFORM_ORGANIZATION_ID));

        Organization platformOrganization = buildOrganization(PlatformConstants.PLATFORM_ORGANIZATION_ID, null, null);
        when(organizationRepository.findById(PlatformConstants.PLATFORM_ORGANIZATION_ID))
                .thenReturn(java.util.Optional.of(platformOrganization));

        when(organizationRepository.findOrganizationOnboardingDates(any(), any(), any()))
                .thenReturn(List.of(), List.of());

        PlatformOnboardingMetricsDTO metrics = analyticsService.obterOnboardingMetricsPlataforma();

        assertNotNull(metrics);
        assertEquals(0, metrics.getTotalOrganizacoes());
        assertEquals(0, metrics.getTotalOrganizacoesAssinadas());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getTempoMedioDiasAteAssinatura());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getPercentualAssinatura7Dias());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getPercentualAssinatura30Dias());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getTaxaConversaoTotal());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getTaxaConversaoUltimos30Dias());
        assertEquals(0, metrics.getTotalTrials());
        assertEquals(0, metrics.getTrialsAtivos());
        assertEquals(0, metrics.getTrialsExpirados());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getTaxaConversaoTrials());
        assertBigDecimalEquals(BigDecimal.ZERO, metrics.getTaxaConversaoTrialsNoPrazo());
    }

    @Test
    void shouldRejectPlatformResumoForNonPlatformOrganization() {
        int organizationId = 99;
        CurrentUser.set(buildUser(organizationId));

        Organization organization = buildOrganization(organizationId, null, null);
        when(organizationRepository.findById(organizationId)).thenReturn(java.util.Optional.of(organization));

        assertThrows(AccessDeniedException.class, () -> analyticsService.obterResumoPlataforma());
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

    private Organization buildOrganization(int id, Float metaMensal, Float metaAnual) {
        Organization organization = Organization.builder()
                .metaMensal(metaMensal)
                .metaAnual(metaAnual)
                .build();
        organization.setId(id);
        return organization;
    }

    private PlatformOrganizationsResumoDTO.TimeSeriesPoint findPointByDate(List<PlatformOrganizationsResumoDTO.TimeSeriesPoint> series,
                                                                           LocalDate date) {
        return series.stream()
                .filter(point -> date.equals(point.getData()))
                .findFirst()
                .orElse(null);
    }

    private OrganizationOnboardingProjection onboarding(LocalDateTime createdAt,
                                                         LocalDate signedAt,
                                                         LocalDate trialInicio,
                                                         LocalDate trialFim,
                                                         TrialType trialTipo) {
        return new OnboardingProjectionStub(createdAt, signedAt, trialInicio, trialFim, trialTipo);
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), () -> "Expected " + expected + " but was " + actual);
    }

    private Venda venda(int organizationId, LocalDate data, BigDecimal valorFinal) {
        Venda venda = new Venda();
        venda.setOrganizationId(organizationId);
        venda.setDataEfetuacao(data);
        venda.setValorFinal(valorFinal);
        return venda;
    }

    private static class OnboardingProjectionStub implements OrganizationOnboardingProjection {
        private final LocalDateTime createdAt;
        private final LocalDate dataAssinatura;
        private final LocalDate trialInicio;
        private final LocalDate trialFim;
        private final TrialType trialTipo;

        private OnboardingProjectionStub(LocalDateTime createdAt,
                                         LocalDate dataAssinatura,
                                         LocalDate trialInicio,
                                         LocalDate trialFim,
                                         TrialType trialTipo) {
            this.createdAt = createdAt;
            this.dataAssinatura = dataAssinatura;
            this.trialInicio = trialInicio;
            this.trialFim = trialFim;
            this.trialTipo = trialTipo;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Override
        public LocalDate getDataAssinatura() {
            return dataAssinatura;
        }

        @Override
        public LocalDate getTrialInicio() {
            return trialInicio;
        }

        @Override
        public LocalDate getTrialFim() {
            return trialFim;
        }

        @Override
        public TrialType getTrialTipo() {
            return trialTipo;
        }
    }
}
