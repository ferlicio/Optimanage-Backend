package com.AIT.Optimanage.Controllers.User;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.User.dto.UserResponse;
import com.AIT.Optimanage.Services.User.UsuarioService;
import com.AIT.Optimanage.Support.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    public ResponseEntity<UserResponse> criarUsuario(@RequestBody @Valid UserRequest request) {
        UserResponse novoUsuario = usuarioService.salvarUsuario(request);
        return created(novoUsuario);
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna uma página de usuários")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<UserResponse>> listarUsuarios(
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) Sort.Direction order,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pagesize", required = false) Integer legacyPageSize) {
        Sort.Direction direction = order != null ? order : Sort.Direction.ASC;
        String sortBy = sort != null ? sort : "id";
        int resolvedPage = PaginationUtils.resolvePage(page);
        int resolvedPageSize = PaginationUtils.resolvePageSize(pageSize, legacyPageSize);
        Pageable pageable = PageRequest.of(resolvedPage, resolvedPageSize, Sort.by(direction, sortBy));
        Page<UserResponse> usuarios = usuarioService.listarUsuarios(pageable);
        return ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário", description = "Retorna um usuário pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<UserResponse> buscarUsuario(@PathVariable Integer id) {
        UserResponse usuario = usuarioService.buscarUsuario(id);
        return ok(usuario);
    }

    @PutMapping("/{id}/plano")
    @Operation(summary = "Atualizar plano do usuário", description = "Atualiza o plano ativo do usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<UserResponse> atualizarPlanoAtivo(@PathVariable Integer id,
                                                            @RequestParam Integer novoPlanoId) {
        UserResponse usuarioAtualizado = usuarioService.atualizarPlanoAtivo(id, novoPlanoId);
        return ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário pelo ID")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Integer id) {
        usuarioService.desativarUsuario(id);
        return noContent();
    }
}

