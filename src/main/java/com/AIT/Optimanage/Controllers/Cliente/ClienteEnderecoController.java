package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Cliente.ClienteEnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteEnderecoController extends V1BaseController {

    private final ClienteEnderecoService clienteEnderecoService;

    @GetMapping("/{idCliente}/enderecos")
    public List<ClienteEndereco> listarEnderecos(@AuthenticationPrincipal User loggedUser, Integer idCliente) {
        return clienteEnderecoService.listarEnderecos(loggedUser, idCliente);
    }

    @PostMapping("/{idCliente}/enderecos")
    public ClienteEndereco cadastrarEndereco(@AuthenticationPrincipal User loggedUser, Integer idCliente, ClienteEndereco endereco) {
        return clienteEnderecoService.cadastrarEndereco(loggedUser, idCliente, endereco);
    }

    @PutMapping("/{idCliente}/enderecos/{idEndereco}")
    public ClienteEndereco editarEndereco(@AuthenticationPrincipal User loggedUser, Integer idCliente, Integer idEndereco, ClienteEndereco endereco) {
        return clienteEnderecoService.editarEndereco(loggedUser, idCliente, idEndereco, endereco);
    }

    @DeleteMapping("/{idCliente}/enderecos/{idEndereco}")
    public void excluirEndereco(@AuthenticationPrincipal User loggedUser, Integer idCliente, Integer idEndereco) {
        clienteEnderecoService.excluirEndereco(loggedUser, idCliente, idEndereco);
    }

}
