package com.AIT.Optimanage.Services.User;

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

    public User salvarUsuario(User usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtivo(true);
        return userRepository.save(usuario);
    }

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public User buscarUsuario(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public User atualizarPlanoAtivo(Integer id, Integer novoPlanoId) {
        User usuario = buscarUsuario(id);
        UserInfo userInfo = userInfoRepository.findByOwnerUser(usuario)
                .orElseThrow(() -> new EntityNotFoundException("Informações do usuário não encontradas"));
        Plano plano = planoRepository.findById(novoPlanoId)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        userInfo.setPlanoAtivoId(plano);
        userInfoRepository.save(userInfo);
        return usuario;
    }

    public void desativarUsuario(Integer id) {
        User usuario = buscarUsuario(id);
        usuario.setAtivo(false);
        userRepository.save(usuario);
    }
}

