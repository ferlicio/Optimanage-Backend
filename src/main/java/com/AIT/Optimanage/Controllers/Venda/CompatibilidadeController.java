package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.CompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Venda.CompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compatibilidades")
@RequiredArgsConstructor
@Tag(name = "Compatibilidades", description = "Operações de compatibilidade")
public class CompatibilidadeController extends V1BaseController {

    private final CompatibilidadeService compatibilidadeService;

    @GetMapping("/{contexto}")
    @Operation(summary = "Listar compatibilidades", description = "Lista compatibilidades por contexto")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<List<Compatibilidade>> getCompatibilidades(@AuthenticationPrincipal User loggedUser, @PathVariable String contexto) {
        List<Compatibilidade> compatibilidades = compatibilidadeService.buscarCompatibilidades(loggedUser, contexto);
        return ok(compatibilidades);
    }

    @PostMapping
    @Operation(summary = "Adicionar compatibilidade", description = "Cria nova compatibilidade")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<Compatibilidade> adicionarCompatibilidade(@AuthenticationPrincipal User loggedUser, @RequestBody CompatibilidadeDTO request) {
        Compatibilidade compatibilidade = compatibilidadeService.adicionarCompatibilidade(loggedUser, request);
        return created(compatibilidade);
    }
}
