package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorEnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/fornecedor")
@RequiredArgsConstructor
@Tag(name = "Fornecedores - Endereços", description = "Gerenciamento de endereços de fornecedores")
public class FornecedorEnderecoController extends V1BaseController {

    private final FornecedorEnderecoService fornecedorEnderecoService;

    @GetMapping("/{idFornecedor}/enderecos")
    @Operation(summary = "Listar endereços", description = "Lista endereços de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<FornecedorEndereco> listarEnderecos(@AuthenticationPrincipal User loggedUser, Integer idFornecedor) {
        return fornecedorEnderecoService.listarEnderecos(loggedUser, idFornecedor);
    }

    @PostMapping("/{idFornecedor}/enderecos")
    @Operation(summary = "Cadastrar endereço", description = "Adiciona endereço a um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public FornecedorEndereco cadastrarEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, FornecedorEndereco endereco) {
        return fornecedorEnderecoService.cadastrarEndereco(loggedUser, idFornecedor, endereco);
    }

    @PutMapping("/{idFornecedor}/enderecos/{idEndereco}")
    @Operation(summary = "Editar endereço", description = "Atualiza endereço de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public FornecedorEndereco editarEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idEndereco, FornecedorEndereco endereco) {
        return fornecedorEnderecoService.editarEndereco(loggedUser, idFornecedor, idEndereco, endereco);
    }

    @DeleteMapping("/{idFornecedor}/enderecos/{idEndereco}")
    @Operation(summary = "Excluir endereço", description = "Remove endereço de um fornecedor")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void excluirEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idEndereco) {
        fornecedorEnderecoService.excluirEndereco(loggedUser, idFornecedor, idEndereco);
    }

}
