package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Services.PlanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planos")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Operações relacionadas a planos")
public class PlanoController extends V1BaseController {

    private final PlanoService planoService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar planos", description = "Retorna uma lista de planos")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<PlanoResponse> listarPlanos() {
        return planoService.listarPlanos();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastrar plano", description = "Cria um novo plano")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public PlanoResponse criarPlano(@RequestBody @Valid PlanoRequest request) {
        return planoService.criarPlano(request);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idPlano}")
    @Operation(summary = "Atualizar plano", description = "Atualiza um plano existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public PlanoResponse atualizarPlano(@PathVariable Integer idPlano,
                                        @RequestBody @Valid PlanoRequest request) {
        return planoService.atualizarPlano(idPlano, request);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idPlano}")
    @Operation(summary = "Remover plano", description = "Remove um plano")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void removerPlano(@PathVariable Integer idPlano) {
        planoService.removerPlano(idPlano);
    }
}

