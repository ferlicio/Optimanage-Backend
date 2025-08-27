package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.ProdutoService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Page<ProdutoResponse>> listarProdutos(@AuthenticationPrincipal User loggedUser,
                                                                @RequestParam(value = "page") Integer page,
                                                                @RequestParam(value = "pageSize") Integer pageSize,
                                                                @RequestParam(value = "sort", required = false) String sort,
                                                                @RequestParam(value = "order", required = false) Sort.Direction order) {
        var pesquisa = Search.builder()
                .page(page)
                .pageSize(pageSize)
                .sort(sort)
                .order(order)
                .build();
        return ok(produtoService.listarProdutos(loggedUser, pesquisa));
    }

    @GetMapping("/{idProduto}")
    @Operation(summary = "Listar produto", description = "Retorna um produto pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ProdutoResponse> listarUmProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        return ok(produtoService.listarUmProduto(loggedUser, idProduto));
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<ProdutoResponse> cadastrarProduto(@AuthenticationPrincipal User loggedUser,
                                                            @RequestBody @Valid ProdutoRequest request) {
        return created(produtoService.cadastrarProduto(loggedUser, request));
    }

    @PutMapping("/{idProduto}")
    @Operation(summary = "Editar produto", description = "Atualiza um produto existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ProdutoResponse> editarProduto(@AuthenticationPrincipal User loggedUser,
                                                         @PathVariable Integer idProduto,
                                                         @RequestBody @Valid ProdutoRequest request) {
        return ok(produtoService.editarProduto(loggedUser, idProduto, request));
    }

    @DeleteMapping("/{idProduto}")
    @Operation(summary = "Excluir produto", description = "Remove um produto")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> excluirProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        produtoService.excluirProduto(loggedUser, idProduto);
        return noContent();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idProduto}/restaurar")
    @Operation(summary = "Restaurar produto", description = "Restaura um produto inativo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<ProdutoResponse> restaurarProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        return ok(produtoService.restaurarProduto(loggedUser, idProduto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idProduto}/permanente")
    @Operation(summary = "Remover produto permanentemente", description = "Exclui definitivamente um produto")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> removerProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        produtoService.removerProduto(loggedUser, idProduto);
        return noContent();
    }
}
