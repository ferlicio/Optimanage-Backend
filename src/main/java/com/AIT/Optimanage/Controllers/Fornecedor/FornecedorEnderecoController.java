package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorEnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores - Endereços", description = "Gerenciamento de endereços de fornecedores")
public class FornecedorEnderecoController extends V1BaseController {

    private final FornecedorEnderecoService fornecedorEnderecoService;

    @GetMapping("/{idFornecedor}/enderecos")
    @Operation(summary = "Listar endereços", description = "Lista endereços de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<List<FornecedorEndereco>> listarEnderecos(@PathVariable("idFornecedor") Integer idFornecedor) {
          return ok(fornecedorEnderecoService.listarEnderecos(idFornecedor));
      }

    @PostMapping("/{idFornecedor}/enderecos")
    @Operation(summary = "Cadastrar endereço", description = "Adiciona endereço a um fornecedor")
    @ApiResponse(responseCode = "201", description = "Criado")
      public ResponseEntity<FornecedorEndereco> cadastrarEndereco(@PathVariable("idFornecedor") Integer idFornecedor,
                                                 @RequestBody @Valid FornecedorEndereco endereco) {
          return created(fornecedorEnderecoService.cadastrarEndereco(idFornecedor, endereco));
      }

    @PutMapping("/{idFornecedor}/enderecos/{idEndereco}")
    @Operation(summary = "Editar endereço", description = "Atualiza endereço de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<FornecedorEndereco> editarEndereco(@PathVariable("idFornecedor") Integer idFornecedor,
                                               @PathVariable("idEndereco") Integer idEndereco,
                                               @RequestBody @Valid FornecedorEndereco endereco) {
          return ok(fornecedorEnderecoService.editarEndereco(idFornecedor, idEndereco, endereco));
      }

    @DeleteMapping("/{idFornecedor}/enderecos/{idEndereco}")
    @Operation(summary = "Excluir endereço", description = "Remove endereço de um fornecedor")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
      public ResponseEntity<Void> excluirEndereco(@PathVariable("idFornecedor") Integer idFornecedor,
                                  @PathVariable("idEndereco") Integer idEndereco) {
          fornecedorEnderecoService.excluirEndereco(idFornecedor, idEndereco);
          return noContent();
      }

}
