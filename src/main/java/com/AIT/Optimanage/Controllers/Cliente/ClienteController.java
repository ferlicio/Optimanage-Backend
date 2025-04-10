package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.Search.ClienteSearch;
import com.AIT.Optimanage.Models.Enums.PageOrder;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;

import com.AIT.Optimanage.Services.Cliente.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController extends V1BaseController {


    private final ClienteService clienteService;

//    @GetMapping
//    public List<Cliente> listarClientes(@AuthenticationPrincipal User loggedUser) {
//        return clienteService.mostrarTodosClientes(loggedUser);
//    }

    @GetMapping
    public List<Cliente> listarClientes(@AuthenticationPrincipal User loggedUser,
                                        @RequestParam(value = "id", required = false) Integer id,
                                        @RequestParam(value = "nome", required = false) String nome,
                                        @RequestParam(value = "estado", required = false) String estado,
                                        @RequestParam(value = "cpfOuCnpj", required = false) String cpfOuCnpj,
                                        @RequestParam(value = "atividade", required = false) Integer atividade,
                                        @RequestParam(value = "tipoPessoa", required = false) TipoPessoa tipoPessoa,
                                        @RequestParam(value = "ativo", required = false) Boolean ativo,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @RequestParam(value = "order", required = false) Sort.Direction order,
                                        @RequestParam(value = "page", required = true) Integer page,
                                        @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        var pesquisa = ClienteSearch.builder()
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
        return clienteService.listarClientes(loggedUser, pesquisa);
    }

    @GetMapping("/{idCliente}")
    public Cliente listarUmCliente(@AuthenticationPrincipal User loggedUser, Integer idCliente) {
        return clienteService.listarUmCliente(loggedUser, idCliente);
    }

    @PostMapping
    public Cliente criarCliente(@AuthenticationPrincipal User loggedUser, @RequestBody Cliente cliente) {
        return clienteService.criarCliente(loggedUser, cliente);
    }

    @PutMapping("/{idCliente}")
    public Cliente editarCliente(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idCliente, @RequestBody Cliente cliente) {
        return clienteService.editarCliente(loggedUser, idCliente, cliente);
    }

    @DeleteMapping("/{idCliente}")
    public void inativarCliente(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idCliente) {
        clienteService.inativarCliente(loggedUser, idCliente);
    }

}
