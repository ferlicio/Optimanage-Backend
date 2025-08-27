package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;

import com.AIT.Optimanage.Services.Cliente.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
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
    @Operation(summary = "Listar clientes", description = "Retorna uma lista de clientes")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<Cliente> listarClientes(@AuthenticationPrincipal User loggedUser,
                                        @RequestParam(value = "id", required = false) Integer id,
                                        @RequestParam(value = "nome", required = false) String nome,
                                        @RequestParam(value = "estado", required = false) String estado,
                                        @RequestParam(value = "cpfOuCnpj", required = false) String cpfOuCnpj,
                                        @RequestParam(value = "atividade", required = false) Integer atividade,
                                        @RequestParam(value = "tipoPessoa", required = false) TipoPessoa tipoPessoa,
                                        @RequestParam(value = "ativo", required = false) Boolean ativo,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @RequestParam(value = "order", required = false) Sort.Direction order,
                                        @RequestParam(value = "page", required = true) Integer page,
                                        @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        var pesquisa = ClienteSearch.builder()
                .id(id)
                .nome(nome)
                .estado(estado)
                .cpfOuCnpj(cpfOuCnpj)
                .atividade(atividade)
                .tipoPessoa(tipoPessoa)
                .ativo(ativo)
                .page(page)
                .pageSize(pagesize)
                .sort(sort)
                .order(order)
                .build();
        return clienteService.listarClientes(loggedUser, pesquisa);
    }

    @GetMapping("/{idCliente}")
    @Operation(summary = "Listar cliente", description = "Retorna um cliente pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Cliente listarUmCliente(@AuthenticationPrincipal User loggedUser,
                                   @PathVariable("idCliente") Integer idCliente) {
        return clienteService.listarUmCliente(loggedUser, idCliente);
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Cliente criarCliente(@AuthenticationPrincipal User loggedUser,
                                @RequestBody @Valid ClienteRequest request) {
        return clienteService.criarCliente(loggedUser, request);
    }

    @PutMapping("/{idCliente}")
    @Operation(summary = "Editar cliente", description = "Atualiza um cliente existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Cliente editarCliente(@AuthenticationPrincipal User loggedUser,
                                 @PathVariable("idCliente") Integer idCliente,
                                 @RequestBody @Valid ClienteRequest request) {
        return clienteService.editarCliente(loggedUser, idCliente, request);
    }

    @DeleteMapping("/{idCliente}")
    @Operation(summary = "Inativar cliente", description = "Inativa um cliente pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void inativarCliente(@AuthenticationPrincipal User loggedUser,
                                @PathVariable("idCliente") Integer idCliente) {
        clienteService.inativarCliente(loggedUser, idCliente);
    }

}
