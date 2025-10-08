package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Controllers.dto.ClienteResponse;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;

import com.AIT.Optimanage.Services.Cliente.ClienteService;
import com.AIT.Optimanage.Support.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Operações relacionadas a clientes")
public class ClienteController extends V1BaseController {


    private final ClienteService clienteService;

//    @GetMapping
//    public List<Cliente> listarClientes(@AuthenticationPrincipal User loggedUser) {
//        return clienteService.mostrarTodosClientes(loggedUser);
//    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna uma página de clientes")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<ClienteResponse>> listarClientes(@RequestParam(value = "id", required = false) Integer id,
                                        @RequestParam(value = "nome", required = false) String nome,
                                        @RequestParam(value = "estado", required = false) String estado,
                                        @RequestParam(value = "cpfOuCnpj", required = false) String cpfOuCnpj,
                                        @RequestParam(value = "atividade", required = false) Integer atividade,
                                        @RequestParam(value = "tipoPessoa", required = false) TipoPessoa tipoPessoa,
                                        @RequestParam(value = "ativo", required = false) Boolean ativo,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @RequestParam(value = "order", required = false) Sort.Direction order,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                        @RequestParam(value = "pagesize", required = false) Integer legacyPageSize) {
        int resolvedPage = PaginationUtils.resolvePage(page);
        int resolvedPageSize = PaginationUtils.resolvePageSize(pageSize, legacyPageSize);
        var pesquisa = ClienteSearch.builder()
                .id(id)
                .nome(nome)
                .estado(estado)
                .cpfOuCnpj(cpfOuCnpj)
                .atividade(atividade)
                .tipoPessoa(tipoPessoa)
                .ativo(ativo)
                .page(resolvedPage)
                .pageSize(resolvedPageSize)
                .sort(sort)
                .order(order)
                .build();
        return ok(clienteService.listarClientes(pesquisa));
    }

    @GetMapping("/{idCliente}")
    @Operation(summary = "Listar cliente", description = "Retorna um cliente pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ClienteResponse> listarUmCliente(@PathVariable("idCliente") Integer idCliente) {
        return ok(clienteService.listarUmClienteResponse(idCliente));
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<ClienteResponse> criarCliente(@RequestBody @Valid ClienteRequest request) {
        return created(clienteService.criarCliente(request));
    }

    @PutMapping("/{idCliente}")
    @Operation(summary = "Editar cliente", description = "Atualiza um cliente existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ClienteResponse> editarCliente(@PathVariable("idCliente") Integer idCliente,
                                                         @RequestBody @Valid ClienteRequest request) {
        return ok(clienteService.editarCliente(idCliente, request));
    }

    @DeleteMapping("/{idCliente}")
    @Operation(summary = "Inativar cliente", description = "Inativa um cliente pelo ID")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> inativarCliente(@PathVariable("idCliente") Integer idCliente) {
        clienteService.inativarCliente(idCliente);
        return noContent();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idCliente}/restaurar")
    @Operation(summary = "Restaurar cliente", description = "Reativa um cliente inativo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ClienteResponse> restaurarCliente(@PathVariable("idCliente") Integer idCliente) {
        return ok(clienteService.reativarCliente(idCliente));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idCliente}/permanente")
    @Operation(summary = "Remover cliente permanentemente", description = "Exclui definitivamente um cliente")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> removerCliente(@PathVariable("idCliente") Integer idCliente) {
        clienteService.removerCliente(idCliente);
        return noContent();
    }

}
