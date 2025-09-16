package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.User.dto.UserResponse;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PlanoRepository planoRepository;
    private final CacheManager cacheManager;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse salvarUsuario(UserRequest request) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Informações do usuário não encontradas"));

        Plano plano = planoRepository.findById(organization.getPlanoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));

        long usuariosAtivos = userRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteUsuarios(plano, usuariosAtivos, 1);

        User usuario = User.builder()
                .nome(request.getNome())
                .sobrenome(request.getSobrenome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole())
                .ativo(true)
                .build();
        usuario.setTenantId(organizationId);
        User salvo = userRepository.save(usuario);
        return toResponse(salvo);
    }

    public Page<UserResponse> listarUsuarios(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse buscarUsuario(Integer id) {
        User usuario = getUsuario(id);
        return toResponse(usuario);
    }

    @Transactional
    public UserResponse atualizarPlanoAtivo(Integer id, Integer novoPlanoId) {
        User usuario = getUsuario(id);
        Organization organization = organizationRepository.findByOwnerUser(usuario)
                .orElseThrow(() -> new EntityNotFoundException("Informações do usuário não encontradas"));
        Plano plano = planoRepository.findById(novoPlanoId)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));

        long usuariosAtivos = userRepository.countByOrganizationIdAndAtivoTrue(organization.getId());
        validarLimiteUsuarios(plano, usuariosAtivos, 0);

        organization.setPlanoAtivoId(plano);
        organizationRepository.save(organization);
        evictPlanoCache(organization.getId());
        return toResponse(usuario);
    }

    public void desativarUsuario(Integer id) {
        User usuario = getUsuario(id);
        usuario.setAtivo(false);
        userRepository.save(usuario);
    }

    private User getUsuario(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    private UserResponse toResponse(User usuario) {
        return UserResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .sobrenome(usuario.getSobrenome())
                .email(usuario.getEmail())
                .ativo(usuario.getAtivo())
                .role(usuario.getRole())
                .build();
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

    private void evictPlanoCache(Integer organizationId) {
        if (organizationId == null) {
            return;
        }
        Cache cache = cacheManager.getCache("planos");
        if (cache != null) {
            cache.evict(organizationId);
        }
    }
}

