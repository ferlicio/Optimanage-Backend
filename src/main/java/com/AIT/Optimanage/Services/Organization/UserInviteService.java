package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Controllers.dto.UserInviteRequest;
import com.AIT.Optimanage.Controllers.dto.UserInviteResponse;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.UserInvite;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.Organization.UserInviteRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInviteService {

    private final OrganizationRepository organizationRepository;
    private final UserInviteRepository inviteRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserInviteResponse gerarConvite(Integer organizationId, UserInviteRequest request, Integer creatorId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        UserInvite invite = UserInvite.builder()
                .code(UUID.randomUUID().toString())
                .role(request.getRole())
                .expiresAt(request.getExpiresAt())
                .email(request.getEmail())
                .createdBy(creator)
                .organization(organization)
                .build();
        invite.setTenantId(organizationId);
        inviteRepository.save(invite);

        return UserInviteResponse.builder()
                .code(invite.getCode())
                .role(invite.getRole())
                .expiresAt(invite.getExpiresAt())
                .email(invite.getEmail())
                .used(false)
                .build();
    }

    @Transactional
    public void revogarConvite(Integer organizationId, String code) {
        UserInvite invite = inviteRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Convite não encontrado"));
        if (!invite.getOrganizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Convite não pertence à organização");
        }
        inviteRepository.delete(invite);
    }

    @Transactional
    public UserInvite validarConvite(String code, String email) {
        UserInvite invite = inviteRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Convite inválido"));
        if (invite.getUsedAt() != null || invite.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Convite inválido");
        }
        if (invite.getEmail() != null && !invite.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Convite inválido");
        }
        return invite;
    }

    @Transactional
    public void marcarComoUsado(UserInvite invite, User user) {
        invite.setUsedAt(Instant.now());
        invite.setUsedBy(user);
        inviteRepository.save(invite);
    }
}
