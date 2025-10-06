package com.AIT.Optimanage.Support;

import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Seeds the database with a platform organization and its owner user on startup.
 */
@Component
@RequiredArgsConstructor
public class PlatformDataInitializer implements ApplicationRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PlanoRepository planoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (organizationRepository.existsById(1)) {
            return;
        }

        TenantContext.setTenantId(1);

        Plano plan = Plano.builder()
                .nome("Platform")
                .valor(0f)
                .duracaoDias(0)
                .qtdAcessos(0)
                .maxUsuarios(0)
                .maxProdutos(0)
                .maxClientes(0)
                .maxFornecedores(0)
                .maxServicos(0)
                .agendaHabilitada(true)
                .recomendacoesHabilitadas(true)
                .pagamentosHabilitados(true)
                .suportePrioritario(true)
                .monitoramentoEstoqueHabilitado(true)
                .integracaoMarketplaceHabilitada(true)
                .build();
        plan.setId(1);
        planoRepository.save(plan);

        Plano trialSevenDays = Plano.builder()
                .nome("Trial 7 dias")
                .valor(0f)
                .duracaoDias(7)
                .qtdAcessos(500)
                .maxUsuarios(5)
                .maxProdutos(120)
                .maxClientes(120)
                .maxFornecedores(60)
                .maxServicos(60)
                .agendaHabilitada(true)
                .recomendacoesHabilitadas(true)
                .pagamentosHabilitados(true)
                .suportePrioritario(true)
                .monitoramentoEstoqueHabilitado(true)
                .metricasProdutoHabilitadas(true)
                .integracaoMarketplaceHabilitada(true)
                .build();
        trialSevenDays.setId(2);
        planoRepository.save(trialSevenDays);

        Plano trialFourteenDays = Plano.builder()
                .nome("Trial 14 dias")
                .valor(0f)
                .duracaoDias(14)
                .qtdAcessos(300)
                .maxUsuarios(4)
                .maxProdutos(80)
                .maxClientes(80)
                .maxFornecedores(40)
                .maxServicos(40)
                .agendaHabilitada(true)
                .recomendacoesHabilitadas(true)
                .pagamentosHabilitados(true)
                .monitoramentoEstoqueHabilitado(true)
                .metricasProdutoHabilitadas(true)
                .integracaoMarketplaceHabilitada(false)
                .build();
        trialFourteenDays.setId(3);
        planoRepository.save(trialFourteenDays);

        Plano trialThirtyDays = Plano.builder()
                .nome("Trial 30 dias")
                .valor(0f)
                .duracaoDias(30)
                .qtdAcessos(150)
                .maxUsuarios(3)
                .maxProdutos(40)
                .maxClientes(40)
                .maxFornecedores(20)
                .maxServicos(20)
                .agendaHabilitada(true)
                .recomendacoesHabilitadas(false)
                .pagamentosHabilitados(false)
                .monitoramentoEstoqueHabilitado(false)
                .metricasProdutoHabilitadas(false)
                .integracaoMarketplaceHabilitada(false)
                .build();
        trialThirtyDays.setId(4);
        planoRepository.save(trialThirtyDays);

        User owner = User.builder()
                .nome("Platform")
                .sobrenome("Owner")
                .email("owner@platform.local")
                .senha(passwordEncoder.encode("changeit"))
                .role(Role.OWNER)
                .ativo(true)
                .build();
        owner.setId(1);
        owner.setTenantId(1);
        userRepository.save(owner);

        Organization organization = Organization.builder()
                .ownerUser(owner)
                .planoAtivoId(plan)
                .cnpj("00000000000000")
                .razaoSocial("Platform")
                .nomeFantasia("Platform")
                .permiteOrcamento(true)
                .dataAssinatura(LocalDate.now())
                .build();
        organization.setId(1);
        organization.setTenantId(1);
        organizationRepository.save(organization);

        owner.setOrganization(organization);
        userRepository.save(owner);

        TenantContext.clear();
    }
}
