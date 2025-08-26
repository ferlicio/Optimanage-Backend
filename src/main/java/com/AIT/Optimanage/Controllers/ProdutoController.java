package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.ProdutoService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Operações relacionadas a produtos")
public class ProdutoController extends V1BaseController {

    private final ProdutoService produtoService;

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna uma lista de produtos")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<Produto> listarProdutos(@AuthenticationPrincipal User loggedUser) {
        return produtoService.listarProdutos(loggedUser);
    }

    @GetMapping("/{idProduto}")
    @Operation(summary = "Listar produto", description = "Retorna um produto pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Produto listarUmProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        return produtoService.listarUmProduto(loggedUser, idProduto);
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Produto cadastrarProduto(@AuthenticationPrincipal User loggedUser,
                                    @RequestBody @Valid ProdutoRequest request) {
        return produtoService.cadastrarProduto(loggedUser, request);
    }

    @PutMapping("/{idProduto}")
    @Operation(summary = "Editar produto", description = "Atualiza um produto existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public Produto editarProduto(@AuthenticationPrincipal User loggedUser,
                                 @PathVariable Integer idProduto,
                                 @RequestBody @Valid ProdutoRequest request) {
        return produtoService.editarProduto(loggedUser, idProduto, request);
    }

    @DeleteMapping("/{idProduto}")
    @Operation(summary = "Excluir produto", description = "Remove um produto")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public void excluirProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        produtoService.excluirProduto(loggedUser, idProduto);
    }
}