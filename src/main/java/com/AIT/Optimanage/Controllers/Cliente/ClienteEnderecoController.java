package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Cliente.ClienteEnderecoService;
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
@Tag(name = "Clientes - Endereços", description = "Gerenciamento de endereços de clientes")
public class ClienteEnderecoController extends V1BaseController {

    private final ClienteEnderecoService clienteEnderecoService;

    @GetMapping("/{idCliente}/enderecos")
    @Operation(summary = "Listar endereços", description = "Lista endereços de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public List<ClienteEndereco> listarEnderecos(@AuthenticationPrincipal User loggedUser,
                                                   @PathVariable("idCliente") Integer idCliente) {
          return clienteEnderecoService.listarEnderecos(loggedUser, idCliente);
      }

    @PostMapping("/{idCliente}/enderecos")
    @Operation(summary = "Cadastrar endereço", description = "Adiciona endereço a um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ClienteEndereco cadastrarEndereco(@AuthenticationPrincipal User loggedUser,
                                              @PathVariable("idCliente") Integer idCliente,
                                              @RequestBody @Valid ClienteEndereco endereco) {
          return clienteEnderecoService.cadastrarEndereco(loggedUser, idCliente, endereco);
      }

    @PutMapping("/{idCliente}/enderecos/{idEndereco}")
    @Operation(summary = "Editar endereço", description = "Atualiza endereço de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ClienteEndereco editarEndereco(@AuthenticationPrincipal User loggedUser,
                                           @PathVariable("idCliente") Integer idCliente,
                                           @PathVariable("idEndereco") Integer idEndereco,
                                           @RequestBody @Valid ClienteEndereco endereco) {
          return clienteEnderecoService.editarEndereco(loggedUser, idCliente, idEndereco, endereco);
      }

    @DeleteMapping("/{idCliente}/enderecos/{idEndereco}")
    @Operation(summary = "Excluir endereço", description = "Remove endereço de um cliente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public void excluirEndereco(@AuthenticationPrincipal User loggedUser,
                                  @PathVariable("idCliente") Integer idCliente,
                                  @PathVariable("idEndereco") Integer idEndereco) {
          clienteEnderecoService.excluirEndereco(loggedUser, idCliente, idEndereco);
      }

}
