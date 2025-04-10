package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController extends V1BaseController {

    private final ProdutoService produtoService;

    @GetMapping
    public List<Produto> listarProdutos(@AuthenticationPrincipal User loggedUser) {
        return produtoService.listarProdutos(loggedUser);
    }

    @GetMapping("/{idProduto}")
    public Produto listarUmProduto(@AuthenticationPrincipal User loggedUser, Integer idProduto) {
        return produtoService.listarUmProduto(loggedUser, idProduto);
    }

    @PostMapping
    public Produto cadastrarProduto(@AuthenticationPrincipal User loggedUser, @RequestBody Produto produto) {
        return produtoService.cadastrarProduto(loggedUser, produto);
    }

    @PutMapping("/{idProduto}")
    public Produto editarProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto, @RequestBody Produto produto) {
        return produtoService.editarProduto(loggedUser, idProduto, produto);
    }

    @DeleteMapping("/{idProduto}")
    public void excluirProduto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idProduto) {
        produtoService.excluirProduto(loggedUser, idProduto);
    }
}