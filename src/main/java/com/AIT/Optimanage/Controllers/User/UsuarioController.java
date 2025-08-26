package com.AIT.Optimanage.Controllers.User;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.User.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UsuarioController extends V1BaseController {

    private final UsuarioService usuarioService;

    @PostMapping("/criar")
    public ResponseEntity<User> criarUsuario(@RequestBody @Valid User usuario) {
        User novoUsuario = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<User>> listarUsuarios() {
        List<User> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> buscarUsuario(@PathVariable Integer id) {
        User usuario = usuarioService.buscarUsuario(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/atualizar-plano")
    public ResponseEntity<User> atualizarPlanoAtivo(@PathVariable Integer id,
                                                    @RequestParam Integer novoPlanoId) {
        User usuarioAtualizado = usuarioService.atualizarPlanoAtivo(id, novoPlanoId);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Integer id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}

