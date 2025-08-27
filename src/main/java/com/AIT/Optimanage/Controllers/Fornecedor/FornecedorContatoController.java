package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorContatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores - Contatos", description = "Gerenciamento de contatos de fornecedores")
public class FornecedorContatoController extends V1BaseController {

    private final FornecedorContatoService fornecedorContatoService;

    @GetMapping("/{idFornecedor}/contatos")
    @Operation(summary = "Listar contatos", description = "Lista contatos de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<List<FornecedorContato>> listarContatos(@AuthenticationPrincipal User loggedUser,
                                                    @PathVariable("idFornecedor") Integer idFornecedor) {
          return ok(fornecedorContatoService.listarContatos(loggedUser, idFornecedor));
      }

    @PostMapping("/{idFornecedor}/contatos")
    @Operation(summary = "Cadastrar contato", description = "Adiciona contato a um fornecedor")
    @ApiResponse(responseCode = "201", description = "Criado")
      public ResponseEntity<FornecedorContato> cadastrarContato(@AuthenticationPrincipal User loggedUser,
                                               @PathVariable("idFornecedor") Integer idFornecedor,
                                               @RequestBody @Valid FornecedorContato contato) {
          return created(fornecedorContatoService.cadastrarContato(loggedUser, idFornecedor, contato));
      }

    @PutMapping("/{idFornecedor}/contatos/{idContato}")
    @Operation(summary = "Editar contato", description = "Atualiza contato de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<FornecedorContato> editarContato(@AuthenticationPrincipal User loggedUser,
                                                             @PathVariable("idFornecedor") Integer idFornecedor,
                                                             @PathVariable("idContato") Integer idContato,
                                                             @RequestBody @Valid FornecedorContato contato) {
          return ok(fornecedorContatoService.editarContato(loggedUser, idFornecedor, idContato, contato));
      }

    @DeleteMapping("/{idFornecedor}/contatos/{idContato}")
    @Operation(summary = "Excluir contato", description = "Remove contato de um fornecedor")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
      public ResponseEntity<Void> excluirContato(@AuthenticationPrincipal User loggedUser,
                                 @PathVariable("idFornecedor") Integer idFornecedor,
                                 @PathVariable("idContato") Integer idContato) {
          fornecedorContatoService.excluirContato(loggedUser, idFornecedor, idContato);
          return noContent();
      }
}
