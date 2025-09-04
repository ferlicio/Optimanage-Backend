package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Services.Cliente.ClienteContatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ClienteContato>> listarContatos(@PathVariable("idCliente") Integer idCliente) {
        return ok(clienteContatoService.listarContatos(idCliente));
    }

    @PostMapping("/{idCliente}/contatos")
    @Operation(summary = "Cadastrar contato", description = "Adiciona contato a um cliente")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<ClienteContato> cadastrarContato(@PathVariable("idCliente") Integer idCliente,
                                                           @RequestBody @Valid ClienteContato contato) {
        return created(clienteContatoService.cadastrarContato(idCliente, contato));
    }

    @PutMapping("/{idCliente}/contatos/{idContato}")
    @Operation(summary = "Editar contato", description = "Atualiza contato de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ClienteContato> editarContato(@PathVariable("idCliente") Integer idCliente,
                                                        @PathVariable("idContato") Integer idContato,
                                                        @RequestBody @Valid ClienteContato contato) {
        return ok(clienteContatoService.editarContato(idCliente, idContato, contato));
    }

    @DeleteMapping("/{idCliente}/contatos/{idContato}")
    @Operation(summary = "Excluir contato", description = "Remove contato de um cliente")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> excluirContato(@PathVariable("idCliente") Integer idCliente,
                                               @PathVariable("idContato") Integer idContato) {
        clienteContatoService.excluirContato(idCliente, idContato);
        return noContent();
    }
}

