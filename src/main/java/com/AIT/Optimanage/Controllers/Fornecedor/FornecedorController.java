package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.Search.FornecedorSearch;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorController extends V1BaseController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<Fornecedor> listarFornecedores(@AuthenticationPrincipal User loggedUser,
                                               @RequestParam(value = "id", required = false) Integer id,
                                               @RequestParam(value = "nome", required = false) String nome,
                                               @RequestParam(value = "cpfOuCnpj", required = false) String cpfOuCnpj,
                                               @RequestParam(value = "atividade", required = false) Integer atividade,
                                               @RequestParam(value = "estado", required = false) String estado,
                                               @RequestParam(value = "tipoPessoa", required = false) TipoPessoa tipoPessoa,
                                               @RequestParam(value = "ativo", required = false) Boolean ativo,
                                               @RequestParam(value = "sort", required = false) String sort,
                                               @RequestParam(value = "order", required = false) Sort.Direction order,
                                               @RequestParam(value = "page", required = true) Integer page,
                                               @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        var pesquisa = FornecedorSearch.builder()
                .id(id)
                .nome(nome)
                .estado(estado)
                .cpfOuCnpj(cpfOuCnpj)
                .atividade(atividade)
                .tipoPessoa(tipoPessoa)
                .ativo(ativo)
                .page(page)
                .pageSize(pagesize)
                .sort(sort)
                .order(order)
                .build();
        return fornecedorService.listarFornecedores(loggedUser, pesquisa);
    }

    @GetMapping("/{idFornecedor}")
    public Fornecedor listarUmFornecedor(@AuthenticationPrincipal User loggedUser, Integer idFornecedor) {
        return fornecedorService.listarUmFornecedor(loggedUser, idFornecedor);
    }

    @PostMapping
    public Fornecedor criarFornecedor(@AuthenticationPrincipal User loggedUser,
                                     @RequestBody @Valid FornecedorRequest request) {
        return fornecedorService.criarFornecedor(loggedUser, request);
    }

    @PutMapping("/{idFornecedor}")
    public Fornecedor editarFornecedor(@AuthenticationPrincipal User loggedUser,
                                       @PathVariable Integer idFornecedor,
                                       @RequestBody @Valid FornecedorRequest request) {
        return fornecedorService.editarFornecedor(loggedUser, idFornecedor, request);
    }

    @DeleteMapping("/{idFornecedor}")
    public void inativarFornecedor(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idFornecedor) {
        fornecedorService.inativarFornecedor(loggedUser, idFornecedor);
    }
}
