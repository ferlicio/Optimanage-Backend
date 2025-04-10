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

@RestController
@RequestMapping("/fornecedor")
@RequiredArgsConstructor
public class FornecedorEnderecoController extends V1BaseController {

    private final FornecedorEnderecoService fornecedorEnderecoService;

    @GetMapping("/{idFornecedor}/enderecos")
    public List<FornecedorEndereco> listarEnderecos(@AuthenticationPrincipal User loggedUser, Integer idFornecedor) {
        return fornecedorEnderecoService.listarEnderecos(loggedUser, idFornecedor);
    }

    @PostMapping("/{idFornecedor}/enderecos")
    public FornecedorEndereco cadastrarEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, FornecedorEndereco endereco) {
        return fornecedorEnderecoService.cadastrarEndereco(loggedUser, idFornecedor, endereco);
    }

    @PutMapping("/{idFornecedor}/enderecos/{idEndereco}")
    public FornecedorEndereco editarEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idEndereco, FornecedorEndereco endereco) {
        return fornecedorEnderecoService.editarEndereco(loggedUser, idFornecedor, idEndereco, endereco);
    }

    @DeleteMapping("/{idFornecedor}/enderecos/{idEndereco}")
    public void excluirEndereco(@AuthenticationPrincipal User loggedUser, Integer idFornecedor, Integer idEndereco) {
        fornecedorEnderecoService.excluirEndereco(loggedUser, idFornecedor, idEndereco);
    }

}
