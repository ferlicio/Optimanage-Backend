package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationResponse;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.TrialType;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.AuditTrailService;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PlanoRepository planoRepository;
    private final AuditTrailService auditTrailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public OrganizationResponse criarOrganizacao(OrganizationRequest request, User creator) {
        validarPermissaoCriacao(creator);
        Plano plano = planoRepository.findById(request.getPlanoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));

        validarLimiteUsuarios(plano, 0, 1);

        boolean planoEhTrial = isTrialPlan(plano);
        LocalDate trialInicio = request.getTrialInicio();
        LocalDate trialFim = request.getTrialFim();
        TrialType trialTipo = request.getTrialTipo();

        validarIntervaloTrial(trialInicio, trialFim);

        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);

            UserRequest ownerReq = request.getOwner();
            User owner = User.builder()
                    .nome(ownerReq.getNome())
                    .sobrenome(ownerReq.getSobrenome())
                    .email(ownerReq.getEmail())
                    .senha(passwordEncoder.encode(ownerReq.getSenha()))
                    .role(Role.OWNER)
                    .ativo(true)
                    .build();
            owner = userRepository.save(owner);

            if (planoEhTrial) {
                if (trialInicio == null) {
                    trialInicio = request.getDataAssinatura();
                }
                if (trialFim == null && trialInicio != null) {
                    Integer duracao = plano.getDuracaoDias();
                    if (duracao != null && duracao > 0) {
                        trialFim = trialInicio.plusDays(duracao);
                    }
                }
                if (trialTipo == null) {
                    trialTipo = TrialType.PLAN_DEFAULT;
                }
            }

            if ((trialInicio != null || trialFim != null) && trialTipo == null) {
                trialTipo = TrialType.CUSTOM;
            }

            validarIntervaloTrial(trialInicio, trialFim);

            Organization organization = Organization.builder()
                    .ownerUser(owner)
                    .planoAtivoId(plano)
                    .cnpj(request.getCnpj())
                    .razaoSocial(request.getRazaoSocial())
                    .nomeFantasia(request.getNomeFantasia())
                    .telefone(request.getTelefone())
                    .email(request.getEmail())
                    .permiteOrcamento(request.getPermiteOrcamento())
                    .dataAssinatura(request.getDataAssinatura())
                    .trialInicio(trialInicio)
                    .trialFim(trialFim)
                    .trialTipo(trialTipo)
                    .build();
            organization = organizationRepository.save(organization);

            Integer orgId = organization.getId();
            userRepository.updateOrganizationTenant(owner.getId(), orgId);
            organizationRepository.updateOrganizationTenant(orgId);

            owner.setTenantId(orgId);
            owner.setOrganization(organization);
            organization.setTenantId(orgId);

            auditTrailService.recordPlanSubscription(
                    organization,
                    null,
                    plano,
                    false,
                    isTrialPlan(plano)
            );

            return OrganizationResponse.builder()
                    .id(orgId)
                    .cnpj(organization.getCnpj())
                    .razaoSocial(organization.getRazaoSocial())
                    .nomeFantasia(organization.getNomeFantasia())
                    .ownerUserId(owner.getId())
                    .trialInicio(organization.getTrialInicio())
                    .trialFim(organization.getTrialFim())
                    .trialTipo(organization.getTrialTipo())
                    .build();
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void validarPermissaoCriacao(User creator) {
        if (creator == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        if (!PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(creator.getTenantId())) {
            throw new AccessDeniedException("Apenas membros da organização base podem criar novas organizações");
        }
        if (creator.getRole() != Role.ADMIN && creator.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Permissões insuficientes para criar organização");
        }
    }

    @Transactional
    public AuthenticationResponse adicionarUsuario(Integer organizationId, UserRequest request) {
        validarOrganizacaoDestino(organizationId);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));

        if (request.getRole() == Role.OWNER) {
            throw new IllegalArgumentException("Role OWNER não é permitido");
        }

        Plano plano = planoRepository.findById(organization.getPlanoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        long usuariosAtivos = userRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteUsuarios(plano, usuariosAtivos, 1);

        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organizationId);
            User user = User.builder()
                    .nome(request.getNome())
                    .sobrenome(request.getSobrenome())
                    .email(request.getEmail())
                    .senha(passwordEncoder.encode(request.getSenha()))
                    .role(request.getRole())
                    .ativo(true)
                    .organization(organization)
                    .build();
            user.setTenantId(organizationId);
            user = userRepository.save(user);

            String token = jwtService.generateToken(
                    Map.<String, Object>of(
                            "tenantId", organizationId,
                            "organizationId", organizationId,
                            "role", user.getRole().name()
                    ),
                    user
            );
            return AuthenticationResponse.builder()
                    .token(token)
                    .build();
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void validarOrganizacaoDestino(Integer organizationId) {
        Integer currentOrganizationId = CurrentUser.getOrganizationId();
        if (currentOrganizationId == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        boolean isPlatformUser = PlatformConstants.PLATFORM_ORGANIZATION_ID.equals(currentOrganizationId);
        if (!isPlatformUser && !organizationId.equals(currentOrganizationId)) {
            throw new AccessDeniedException("Usuário não pertence à organização");
        }
    }

    private void validarLimiteUsuarios(Plano plano, long usuariosAtivos, int novosUsuarios) {
        if (plano == null) {
            throw new EntityNotFoundException("Plano não encontrado");
        }
        Integer limite = plano.getMaxUsuarios();
        if (limite != null && limite > 0 && usuariosAtivos + novosUsuarios > limite) {
            throw new IllegalStateException("Limite de usuários do plano atingido");
        }
    }

    private boolean isTrialPlan(Plano plano) {
        if (plano == null) {
            return false;
        }
        Float valor = plano.getValor();
        return valor == null || Float.compare(valor, 0f) <= 0;
    }

    private void validarIntervaloTrial(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data de fim do trial não pode ser anterior ao início");
        }
    }
}
