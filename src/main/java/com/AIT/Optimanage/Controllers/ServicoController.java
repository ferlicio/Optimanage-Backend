package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.ServicoService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Operações relacionadas a serviços")
public class ServicoController extends V1BaseController {

    private final ServicoService servicoService;

    @GetMapping
    @Operation(summary = "Listar serviços", description = "Retorna uma lista de serviços")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Page<ServicoResponse> listarServicos(@AuthenticationPrincipal User loggedUser,
                                                @RequestParam(value = "page") Integer page,
                                                @RequestParam(value = "pageSize") Integer pageSize,
                                                @RequestParam(value = "sort", required = false) String sort,
                                                @RequestParam(value = "order", required = false) Sort.Direction order) {
        var pesquisa = Search.builder()
                .page(page)
                .pageSize(pageSize)
                .sort(sort)
                .order(order)
                .build();
        return servicoService.listarServicos(loggedUser, pesquisa);
    }

    @GetMapping("/{idServico}")
    @Operation(summary = "Listar serviço", description = "Retorna um serviço pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ServicoResponse listarUmServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico) {
        return servicoService.listarUmServico(loggedUser, idServico);
    }

    @PostMapping
    @Operation(summary = "Cadastrar serviço", description = "Cria um novo serviço")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ServicoResponse cadastrarServico(@AuthenticationPrincipal User loggedUser,
                                    @RequestBody @Valid ServicoRequest request) {
        return servicoService.cadastrarServico(loggedUser, request);
    }

    @PutMapping("/{idServico}")
    @Operation(summary = "Editar serviço", description = "Atualiza um serviço existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ServicoResponse editarServico(@AuthenticationPrincipal User loggedUser,
                                 @PathVariable Integer idServico,
                                 @RequestBody @Valid ServicoRequest request) {
        return servicoService.editarServico(loggedUser, idServico, request);
    }

    @DeleteMapping("/{idServico}")
    @Operation(summary = "Excluir serviço", description = "Remove um serviço")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void excluirServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico) {
        servicoService.excluirServico(loggedUser, idServico);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idServico}/restaurar")
    @Operation(summary = "Restaurar serviço", description = "Restaura um serviço inativo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ServicoResponse restaurarServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico) {
        return servicoService.restaurarServico(loggedUser, idServico);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idServico}/permanente")
    @Operation(summary = "Remover serviço permanentemente", description = "Exclui definitivamente um serviço")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void removerServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico) {
        servicoService.removerServico(loggedUser, idServico);
    }
}

