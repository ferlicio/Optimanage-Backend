package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.User.dto.UserResponse;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.User.UserInfo;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.User.UserInfoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final PlanoRepository planoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse salvarUsuario(UserRequest request) {
        User usuario = User.builder()
                .nome(request.getNome())
                .sobrenome(request.getSobrenome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole())
                .ativo(true)
                .build();
        User salvo = userRepository.save(usuario);
        return toResponse(salvo);
    }

    public List<UserResponse> listarUsuarios() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse buscarUsuario(Integer id) {
        User usuario = getUsuario(id);
        return toResponse(usuario);
    }

    @Transactional
    public UserResponse atualizarPlanoAtivo(Integer id, Integer novoPlanoId) {
        User usuario = getUsuario(id);
        UserInfo userInfo = userInfoRepository.findByOwnerUser(usuario)
                .orElseThrow(() -> new EntityNotFoundException("Informações do usuário não encontradas"));
        Plano plano = planoRepository.findById(novoPlanoId)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        userInfo.setPlanoAtivoId(plano);
        userInfoRepository.save(userInfo);
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
}

