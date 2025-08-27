package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Venda.ContextoCompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/contextos")
@RequiredArgsConstructor
@Tag(name = "Contextos", description = "Operações de contexto de compatibilidade")
public class ContextoCompatibilidadeController extends V1BaseController {

    private final ContextoCompatibilidadeService contextoService;

    @GetMapping
    @Operation(summary = "Listar contextos", description = "Retorna os contextos de compatibilidade")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ContextoCompatibilidade> listarContextos(@AuthenticationPrincipal User loggedUser) {
        return ok(contextoService.listarContextos(loggedUser));
    }

    @GetMapping("/{idContexto}")
    @Operation(summary = "Listar contexto", description = "Retorna um contexto pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ContextoCompatibilidade> listarUmContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto) {
        return ok(contextoService.listarUmContexto(loggedUser, idContexto));
    }

    @PostMapping
    @Operation(summary = "Criar contexto", description = "Cria um novo contexto")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<ContextoCompatibilidade> criarContexto(@AuthenticationPrincipal User loggedUser, @RequestBody ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = contextoService.criarContexto(loggedUser, request);
        return created(contexto);
    }

    @PutMapping("/{idContexto}")
    @Operation(summary = "Editar contexto", description = "Atualiza um contexto existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ContextoCompatibilidade> editarContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto, @RequestBody ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = contextoService.editarContexto(loggedUser, idContexto, request);
        return ok(contexto);
    }

    @DeleteMapping("/{idContexto}")
    @Operation(summary = "Excluir contexto", description = "Remove um contexto")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> excluirContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto) {
        contextoService.excluirContexto(loggedUser, idContexto);
        return noContent();
    }
}
