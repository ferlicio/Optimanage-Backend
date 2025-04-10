package com.AIT.Optimanage.Controllers.Fornecedor;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorContatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorContatoController extends V1BaseController {

    private final FornecedorContatoService fornecedorContatoService;

    @GetMapping("/{idFornecedor}/contatos")
    public List<FornecedorContato> listarContatos(@AuthenticationPrincipal User loggedUser, Integer idFornecedor) {
        return fornecedorContatoService.listarContatos(loggedUser, idFornecedor);
    }

    @PostMapping("/{idFornecedor}/contatos")
    public FornecedorContato cadastrarContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, FornecedorContato contato) {
        return fornecedorContatoService.cadastrarContato(loggedUser, idFornecedor, contato);
    }

    @PutMapping("/{idFornecedor}/contatos/{idContato}")
    public FornecedorContato editarContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idContato, FornecedorContato contato) {
        return fornecedorContatoService.editarContato(loggedUser, idFornecedor, idContato, contato);
    }

    @DeleteMapping("/{idFornecedor}/contatos/{idContato}")
    public void excluirContato(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idContato) {
        fornecedorContatoService.excluirContato(loggedUser, idFornecedor, idContato);
    }
}
