package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Services.Cliente.ClienteEnderecoService;
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
@Tag(name = "Clientes - Endereços", description = "Gerenciamento de endereços de clientes")
public class ClienteEnderecoController extends V1BaseController {

    private final ClienteEnderecoService clienteEnderecoService;

    @GetMapping("/{idCliente}/enderecos")
    @Operation(summary = "Listar endereços", description = "Lista endereços de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<List<ClienteEndereco>> listarEnderecos(@PathVariable("idCliente") Integer idCliente) {
          return ok(clienteEnderecoService.listarEnderecos(idCliente));
      }

    @PostMapping("/{idCliente}/enderecos")
    @Operation(summary = "Cadastrar endereço", description = "Adiciona endereço a um cliente")
    @ApiResponse(responseCode = "201", description = "Criado")
      public ResponseEntity<ClienteEndereco> cadastrarEndereco(@PathVariable("idCliente") Integer idCliente,
                                              @RequestBody @Valid ClienteEndereco endereco) {
          return created(clienteEnderecoService.cadastrarEndereco(idCliente, endereco));
      }

    @PutMapping("/{idCliente}/enderecos/{idEndereco}")
    @Operation(summary = "Editar endereço", description = "Atualiza endereço de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<ClienteEndereco> editarEndereco(@PathVariable("idCliente") Integer idCliente,
                                                           @PathVariable("idEndereco") Integer idEndereco,
                                                           @RequestBody @Valid ClienteEndereco endereco) {
          return ok(clienteEnderecoService.editarEndereco(idCliente, idEndereco, endereco));
      }

    @DeleteMapping("/{idCliente}/enderecos/{idEndereco}")
    @Operation(summary = "Excluir endereço", description = "Remove endereço de um cliente")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
      public ResponseEntity<Void> excluirEndereco(@PathVariable("idCliente") Integer idCliente,
                                  @PathVariable("idEndereco") Integer idEndereco) {
          clienteEnderecoService.excluirEndereco(idCliente, idEndereco);
          return noContent();
      }

}
