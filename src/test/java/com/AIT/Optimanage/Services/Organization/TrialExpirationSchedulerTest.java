package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Models.Audit.AuditTrail;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.TrialType;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Audit.AuditTrailRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import com.AIT.Optimanage.Support.PlatformDataInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TrialExpirationSchedulerTest {

    @Autowired
    private TrialExpirationScheduler scheduler;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private PlatformDataInitializer platformDataInitializer;

    private Plano basePlan;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);
        ensureAuditTrailTableExists();
        resetDatabase();
        basePlan = planoRepository.save(buildViewOnlyPlan());
        initializePlatformTenant(basePlan);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldDowngradeExpiredTrialsAndRecordAudit() {
        Plano basePlan = this.basePlan;
        Plano trialPlan = createTrialPlan("Trial 7 dias", 7, 0f);

        User owner = createOwnerUser();
        Organization organization = Organization.builder()
                .ownerUser(owner)
                .planoAtivoId(trialPlan)
                .cnpj("12345678901234")
                .razaoSocial("Trial Corp")
                .nomeFantasia("Trial Corp")
                .permiteOrcamento(true)
                .dataAssinatura(LocalDate.now().minusDays(10))
                .trialInicio(LocalDate.now().minusDays(10))
                .trialFim(LocalDate.now().minusDays(1))
                .trialTipo(TrialType.PLAN_DEFAULT)
                .build();
        organization = organizationRepository.save(organization);
        Integer organizationId = organization.getId();
        jdbcTemplate.update("UPDATE \"organization\" SET \"organization_id\"=? WHERE \"id\"=?", organizationId, organizationId);

        owner.setOrganization(organization);
        owner.setTenantId(organizationId);
        userRepository.save(owner);

        primePlanCache(organizationId);

        scheduler.downgradeExpiredTrials();

        Organization updated = organizationRepository.findById(organizationId).orElseThrow();
        Plano assignedPlan = planoRepository.findById(updated.getPlanoAtivoId()).orElseThrow();
        assertThat(assignedPlan.getNome()).isEqualTo(PlatformConstants.VIEW_ONLY_PLAN_NAME);
        assertThat(assignedPlan.getTenantId()).isEqualTo(organizationId);
        assertThat(assignedPlan.getValor()).isEqualTo(basePlan.getValor());
        assertThat(assignedPlan.getDuracaoDias()).isEqualTo(basePlan.getDuracaoDias());
        assertThat(updated.getTrialInicio()).isNull();
        assertThat(updated.getTrialFim()).isNull();
        assertThat(updated.getTrialTipo()).isNull();

        assertPlanCacheEvicted(organizationId);

        List<AuditTrail> entries = auditTrailRepository.findAll().stream()
                .filter(entry -> organizationId.equals(entry.getTenantId()))
                .toList();
        assertThat(entries).hasSize(1);
        AuditTrail entry = entries.get(0);
        assertThat(entry.getTenantId()).isEqualTo(organizationId);
        assertThat(entry.getEntityId()).isEqualTo(organizationId);
        assertThat(entry.getAction()).isEqualTo("ALTERACAO_ASSINATURA_PLANO");
        assertThat(entry.getDetails()).contains("Trial 7 dias");
        assertThat(entry.getDetails()).contains(PlatformConstants.VIEW_ONLY_PLAN_NAME);
    }

    @Test
    void shouldDowngradeTrialsWithoutStoredEndDateWhenPlanDefinesDuration() {
        Plano basePlan = this.basePlan;
        Plano trialPlan = createTrialPlan("Trial 30 dias", 30, 0f);

        User owner = createOwnerUser();
        Organization organization = Organization.builder()
                .ownerUser(owner)
                .planoAtivoId(trialPlan)
                .cnpj("56789012345678")
                .razaoSocial("Duration Corp")
                .nomeFantasia("Duration Corp")
                .permiteOrcamento(true)
                .dataAssinatura(LocalDate.now().minusDays(40))
                .trialInicio(LocalDate.now().minusDays(35))
                .trialFim(null)
                .trialTipo(TrialType.PLAN_DEFAULT)
                .build();
        organization = organizationRepository.save(organization);
        Integer organizationId = organization.getId();
        jdbcTemplate.update("UPDATE \"organization\" SET \"organization_id\"=? WHERE \"id\"=?", organizationId, organizationId);

        owner.setOrganization(organization);
        owner.setTenantId(organizationId);
        userRepository.save(owner);

        scheduler.downgradeExpiredTrials();

        Organization updated = organizationRepository.findById(organizationId).orElseThrow();
        Plano assignedPlan = planoRepository.findById(updated.getPlanoAtivoId()).orElseThrow();
        assertThat(assignedPlan.getNome()).isEqualTo(PlatformConstants.VIEW_ONLY_PLAN_NAME);
        assertThat(assignedPlan.getTenantId()).isEqualTo(organizationId);
        assertThat(assignedPlan.getValor()).isEqualTo(basePlan.getValor());
        assertThat(assignedPlan.getDuracaoDias()).isEqualTo(basePlan.getDuracaoDias());
        assertThat(updated.getTrialInicio()).isNull();
        assertThat(updated.getTrialFim()).isNull();
        assertThat(updated.getTrialTipo()).isNull();

        List<AuditTrail> entries = auditTrailRepository.findAll().stream()
                .filter(entry -> organizationId.equals(entry.getTenantId()))
                .toList();
        assertThat(entries).hasSize(1);
    }

    private void primePlanCache(Integer organizationId) {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organizationId);
            Cache cache = cacheManager.getCache("planos");
            if (cache != null) {
                cache.put(organizationId, "cached");
            }
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void assertPlanCacheEvicted(Integer organizationId) {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organizationId);
            Cache cache = cacheManager.getCache("planos");
            assertThat(cache).isNotNull();
            assertThat(cache.get(organizationId)).isNull();
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private Plano createTrialPlan(String name, int durationDays, float value) {
        Plano plan = Plano.builder()
                .nome(name)
                .valor(value)
                .duracaoDias(durationDays)
                .qtdAcessos(100)
                .maxUsuarios(3)
                .maxProdutos(10)
                .maxClientes(10)
                .maxFornecedores(5)
                .maxServicos(5)
                .agendaHabilitada(true)
                .recomendacoesHabilitadas(true)
                .pagamentosHabilitados(false)
                .suportePrioritario(false)
                .monitoramentoEstoqueHabilitado(false)
                .metricasProdutoHabilitadas(false)
                .integracaoMarketplaceHabilitada(false)
                .build();
        return planoRepository.save(plan);
    }

    private User createOwnerUser() {
        User owner = User.builder()
                .nome("Owner")
                .sobrenome("User")
                .email("owner" + System.nanoTime() + "@example.com")
                .senha("secret")
                .role(Role.OWNER)
                .ativo(true)
                .build();
        return userRepository.save(owner);
    }

    private Plano buildViewOnlyPlan() {
        return Plano.builder()
                .nome(PlatformConstants.VIEW_ONLY_PLAN_NAME)
                .valor(0f)
                .duracaoDias(0)
                .qtdAcessos(0)
                .maxUsuarios(0)
                .maxProdutos(0)
                .maxClientes(0)
                .maxFornecedores(0)
                .maxServicos(0)
                .agendaHabilitada(false)
                .recomendacoesHabilitadas(false)
                .pagamentosHabilitados(false)
                .suportePrioritario(false)
                .monitoramentoEstoqueHabilitado(false)
                .metricasProdutoHabilitadas(false)
                .integracaoMarketplaceHabilitada(false)
                .build();
    }

    private void resetDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        executeSilently("TRUNCATE TABLE \"audit_trail\"");
        executeSilently("TRUNCATE TABLE \"organization\"");
        executeSilently("TRUNCATE TABLE \"user\"");
        executeSilently("TRUNCATE TABLE \"plano\"");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void initializePlatformTenant(Plano plan) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO \"user\" (\"id\", \"organization_id\", \"nome\", \"sobrenome\", \"email\", \"senha\", " +
                        "\"ativo\", \"role\", \"two_factor_enabled\", \"failed_attempts\", \"created_at\", \"updated_at\") " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                PlatformConstants.PLATFORM_ORGANIZATION_ID,
                PlatformConstants.PLATFORM_ORGANIZATION_ID,
                "Platform",
                "Owner",
                "owner@platform.local",
                "secret",
                true,
                Role.OWNER.name(),
                false,
                0,
                java.sql.Timestamp.valueOf(now),
                java.sql.Timestamp.valueOf(now)
        );
        jdbcTemplate.update(
                "INSERT INTO \"organization\" (\"id\", \"owner_user_id\", \"tipo_acesso_id\", \"cnpj\", \"razao_social\", " +
                        "\"nome_fantasia\", \"permite_orcamento\", \"data_assinatura\", \"organization_id\", \"created_at\", \"updated_at\") " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                PlatformConstants.PLATFORM_ORGANIZATION_ID,
                PlatformConstants.PLATFORM_ORGANIZATION_ID,
                plan.getId(),
                "00000000000000",
                "Platform",
                "Platform",
                true,
                java.sql.Date.valueOf(LocalDate.now()),
                PlatformConstants.PLATFORM_ORGANIZATION_ID,
                java.sql.Timestamp.valueOf(now),
                java.sql.Timestamp.valueOf(now)
        );
        jdbcTemplate.execute("ALTER TABLE \"user\" ALTER COLUMN \"id\" RESTART WITH " + (PlatformConstants.PLATFORM_ORGANIZATION_ID + 1));
        jdbcTemplate.execute("ALTER TABLE \"organization\" ALTER COLUMN \"id\" RESTART WITH " + (PlatformConstants.PLATFORM_ORGANIZATION_ID + 1));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void executeSilently(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
        }
    }

    private void ensureAuditTrailTableExists() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"audit_trail\" (" +
                        "\"id\" INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                        "\"organization_id\" INT NOT NULL, " +
                        "\"entity_type\" VARCHAR(100) NOT NULL, " +
                        "\"entity_id\" INT NOT NULL, " +
                        "\"action\" VARCHAR(150) NOT NULL, " +
                        "\"details\" TEXT, " +
                        "\"created_by\" INT, " +
                        "\"created_at\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                        "\"updated_by\" INT, " +
                        "\"updated_at\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                        ")"
        );
    }
}
