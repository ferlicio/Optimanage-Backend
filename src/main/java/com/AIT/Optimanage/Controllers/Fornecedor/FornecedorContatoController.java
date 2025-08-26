package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorContatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public List<FornecedorContato> listarContatos(@AuthenticationPrincipal User loggedUser, Integer idFornecedor) {
        return fornecedorContatoService.listarContatos(loggedUser, idFornecedor);
    }

    @PostMapping("/{idFornecedor}/contatos")
    @Operation(summary = "Cadastrar contato", description = "Adiciona contato a um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public FornecedorContato cadastrarContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, FornecedorContato contato) {
        return fornecedorContatoService.cadastrarContato(loggedUser, idFornecedor, contato);
    }

    @PutMapping("/{idFornecedor}/contatos/{idContato}")
    @Operation(summary = "Editar contato", description = "Atualiza contato de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public FornecedorContato editarContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idContato, FornecedorContato contato) {
        return fornecedorContatoService.editarContato(loggedUser, idFornecedor, idContato, contato);
    }

    @DeleteMapping("/{idFornecedor}/contatos/{idContato}")
    @Operation(summary = "Excluir contato", description = "Remove contato de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void excluirContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idContato) {
        fornecedorContatoService.excluirContato(loggedUser, idFornecedor, idContato);
    }
}
