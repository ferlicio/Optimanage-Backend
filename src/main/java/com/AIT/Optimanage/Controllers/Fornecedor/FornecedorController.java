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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores", description = "Operações relacionadas a fornecedores")
public class FornecedorController extends V1BaseController {

    private final FornecedorService fornecedorService;

    @GetMapping
    @Operation(summary = "Listar fornecedores", description = "Retorna uma página de fornecedores")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<Fornecedor>> listarFornecedores(@AuthenticationPrincipal User loggedUser,
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
        return ok(fornecedorService.listarFornecedores(loggedUser, pesquisa));
    }

    @GetMapping("/{idFornecedor}")
    @Operation(summary = "Listar fornecedor", description = "Retorna um fornecedor pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Fornecedor> listarUmFornecedor(@AuthenticationPrincipal User loggedUser,
                                                         @PathVariable("idFornecedor") Integer idFornecedor) {
        return ok(fornecedorService.listarUmFornecedor(loggedUser, idFornecedor));
    }

    @PostMapping
    @Operation(summary = "Criar fornecedor", description = "Cria um novo fornecedor")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<Fornecedor> criarFornecedor(@AuthenticationPrincipal User loggedUser,
                                                     @RequestBody @Valid FornecedorRequest request) {
        return created(fornecedorService.criarFornecedor(loggedUser, request));
    }

    @PutMapping("/{idFornecedor}")
    @Operation(summary = "Editar fornecedor", description = "Atualiza um fornecedor existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Fornecedor> editarFornecedor(@AuthenticationPrincipal User loggedUser,
                                                       @PathVariable("idFornecedor") Integer idFornecedor,
                                                       @RequestBody @Valid FornecedorRequest request) {
        return ok(fornecedorService.editarFornecedor(loggedUser, idFornecedor, request));
    }

    @DeleteMapping("/{idFornecedor}")
    @Operation(summary = "Inativar fornecedor", description = "Inativa um fornecedor pelo ID")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> inativarFornecedor(@AuthenticationPrincipal User loggedUser,
                                                   @PathVariable("idFornecedor") Integer idFornecedor) {
        fornecedorService.inativarFornecedor(loggedUser, idFornecedor);
        return noContent();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{idFornecedor}/restaurar")
    @Operation(summary = "Restaurar fornecedor", description = "Reativa um fornecedor inativo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Fornecedor> restaurarFornecedor(@AuthenticationPrincipal User loggedUser,
                                                          @PathVariable("idFornecedor") Integer idFornecedor) {
        return ok(fornecedorService.reativarFornecedor(loggedUser, idFornecedor));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idFornecedor}/permanente")
    @Operation(summary = "Remover fornecedor permanentemente", description = "Exclui definitivamente um fornecedor")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    public ResponseEntity<Void> removerFornecedor(@AuthenticationPrincipal User loggedUser,
                                                  @PathVariable("idFornecedor") Integer idFornecedor) {
        fornecedorService.removerFornecedor(loggedUser, idFornecedor);
        return noContent();
    }
}
