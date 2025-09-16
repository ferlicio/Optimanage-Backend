package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.PlanoQuotaResponse;
import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Services.PlanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/planos")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Operações relacionadas a planos")
public class PlanoController extends V1BaseController {

    private final PlanoService planoService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar plano atual", description = "Retorna o plano ativo da organização com o uso das quotas")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<PlanoQuotaResponse> obterPlanoAtual(
            @AuthenticationPrincipal com.AIT.Optimanage.Models.User.User loggedUser) {
        return ok(planoService.obterPlanoAtual(loggedUser));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar planos", description = "Retorna uma lista de planos")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<List<PlanoResponse>> listarPlanos() {
        return ok(planoService.listarPlanos());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastrar plano", description = "Cria um novo plano")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<PlanoResponse> criarPlano(@RequestBody @Valid PlanoRequest request) {
        return created(planoService.criarPlano(request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idPlano}")
    @Operation(summary = "Atualizar plano", description = "Atualiza um plano existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<PlanoResponse> atualizarPlano(@PathVariable Integer idPlano,
                                                        @RequestBody @Valid PlanoRequest request) {
        return ok(planoService.atualizarPlano(idPlano, request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idPlano}")
    @Operation(summary = "Remover plano", description = "Remove um plano")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> removerPlano(@PathVariable Integer idPlano) {
        planoService.removerPlano(idPlano);
        return noContent();
    }
}

