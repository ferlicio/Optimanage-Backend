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
                .build();
        plan.setId(1);
        planoRepository.save(plan);

        User owner = User.builder()
                .nome("Platform")
                .sobrenome("Owner")
                .email("owner@platform.local")
                .senha(passwordEncoder.encode("changeit"))
                .role(Role.OWNER)
                .ativo(true)
                .build();
        owner.setId(1);
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
        organizationRepository.save(organization);

        owner.setOrganization(organization);
        userRepository.save(owner);

        TenantContext.clear();
    }
}
