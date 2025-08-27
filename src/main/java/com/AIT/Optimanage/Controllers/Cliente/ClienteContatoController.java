package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Cliente.ClienteContatoService;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Clientes - Contatos", description = "Gerenciamento de contatos de clientes")
public class ClienteContatoController extends V1BaseController {

    private final ClienteContatoService clienteContatoService;

    @GetMapping("/{idCliente}/contatos")
    @Operation(summary = "Listar contatos", description = "Lista contatos de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<ClienteContato> listarContatos(@AuthenticationPrincipal User loggedUser,
                                               @PathVariable("idCliente") Integer idCliente) {
        return clienteContatoService.listarContatos(loggedUser, idCliente);
    }

    @PostMapping("/{idCliente}/contatos")
    @Operation(summary = "Cadastrar contato", description = "Adiciona contato a um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ClienteContato cadastrarContato(@AuthenticationPrincipal User loggedUser,
                                          @PathVariable("idCliente") Integer idCliente,
                                          @RequestBody @Valid ClienteContato contato) {
        return clienteContatoService.cadastrarContato(loggedUser, idCliente, contato);
    }

    @PutMapping("/{idCliente}/contatos/{idContato}")
    @Operation(summary = "Editar contato", description = "Atualiza contato de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ClienteContato editarContato(@AuthenticationPrincipal User loggedUser,
                                        @PathVariable("idCliente") Integer idCliente,
                                        @PathVariable("idContato") Integer idContato,
                                        @RequestBody @Valid ClienteContato contato) {
        return clienteContatoService.editarContato(loggedUser, idCliente, idContato, contato);
    }

    @DeleteMapping("/{idCliente}/contatos/{idContato}")
    @Operation(summary = "Excluir contato", description = "Remove contato de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void excluirContato(@AuthenticationPrincipal User loggedUser,
                               @PathVariable("idCliente") Integer idCliente,
                               @PathVariable("idContato") Integer idContato) {
        clienteContatoService.excluirContato(loggedUser, idCliente, idContato);
    }
}

