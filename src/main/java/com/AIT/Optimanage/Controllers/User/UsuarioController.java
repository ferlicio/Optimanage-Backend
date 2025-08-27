package com.AIT.Optimanage.Controllers.User;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.User.dto.UserResponse;
import com.AIT.Optimanage.Services.User.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = "Usuários", description = "Operações relacionadas a usuários")
public class UsuarioController extends V1BaseController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UserResponse> criarUsuario(@RequestBody @Valid UserRequest request) {
        UserResponse novoUsuario = usuarioService.salvarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listarUsuarios(
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) Sort.Direction order,
            @RequestParam(value = "page", required = true) Integer page,
            @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        Sort.Direction direction = order != null ? order : Sort.Direction.ASC;
        String sortBy = sort != null ? sort : "id";
        Pageable pageable = PageRequest.of(page, pagesize, Sort.by(direction, sortBy));
        Page<UserResponse> usuarios = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> buscarUsuario(@PathVariable Integer id) {
        UserResponse usuario = usuarioService.buscarUsuario(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/plano")
    public ResponseEntity<UserResponse> atualizarPlanoAtivo(@PathVariable Integer id,
                                                            @RequestParam Integer novoPlanoId) {
        UserResponse usuarioAtualizado = usuarioService.atualizarPlanoAtivo(id, novoPlanoId);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Integer id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}

