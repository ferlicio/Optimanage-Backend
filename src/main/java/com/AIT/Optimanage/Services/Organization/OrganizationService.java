package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationResponse;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PlanoRepository planoRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public OrganizationResponse criarOrganizacao(OrganizationRequest request) {
        // Create owner user first
        UserRequest ownerReq = request.getOwner();
        User owner = User.builder()
                .nome(ownerReq.getNome())
                .sobrenome(ownerReq.getSobrenome())
                .email(ownerReq.getEmail())
                .senha(passwordEncoder.encode(ownerReq.getSenha()))
                .role(Role.OWNER)
                .ativo(true)
                .build();
        // Temporarily set tenant to 0 until organization is persisted
        owner.setTenantId(0);
        owner = userRepository.save(owner);

        Plano plano = planoRepository.findById(request.getPlanoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));

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
                .build();
        organization.setTenantId(0);
        organization = organizationRepository.save(organization);

        // Update tenant for owner and organization to the generated id
        Integer orgId = organization.getId();
        owner.setTenantId(orgId);
        owner.setOrganization(organization);
        userRepository.save(owner);
        organization.setTenantId(orgId);
        organizationRepository.save(organization);

        return OrganizationResponse.builder()
                .id(orgId)
                .cnpj(organization.getCnpj())
                .razaoSocial(organization.getRazaoSocial())
                .nomeFantasia(organization.getNomeFantasia())
                .ownerUserId(owner.getId())
                .build();
    }

    @Transactional
    public AuthenticationResponse adicionarUsuario(Integer organizationId, UserRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));

        if (request.getRole() == Role.OWNER) {
            throw new IllegalArgumentException("Role OWNER não é permitido");
        }

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
        TenantContext.clear();
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
