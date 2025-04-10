package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Cliente.ClienteContatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteContatoController extends V1BaseController {

    private final ClienteContatoService clienteContatoService;

    @GetMapping("/{idCliente}/contatos")
    public List<ClienteContato> listarContatos(@AuthenticationPrincipal User loggedUser, Integer idCliente) {
        return clienteContatoService.listarContatos(loggedUser, idCliente);
    }

    @PostMapping("/{idCliente}/contatos")
    public ClienteContato cadastrarContato(@AuthenticationPrincipal User loggedUser, Integer idCliente, ClienteContato contato) {
        return clienteContatoService.cadastrarContato(loggedUser, idCliente, contato);
    }

    @PutMapping("/{idCliente}/contatos/{idContato}")
    public ClienteContato editarContato(@AuthenticationPrincipal User loggedUser, Integer idCliente, Integer idContato, ClienteContato contato) {
        return clienteContatoService.editarContato(loggedUser, idCliente, idContato, contato);
    }

    @DeleteMapping("/{idCliente}/contatos/{idContato}")
    public void excluirContato(@AuthenticationPrincipal User loggedUser, Integer idCliente, Integer idContato) {
        clienteContatoService.excluirContato(loggedUser, idCliente, idContato);
    }
}
