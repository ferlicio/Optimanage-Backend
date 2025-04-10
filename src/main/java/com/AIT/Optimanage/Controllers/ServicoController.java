package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController extends V1BaseController {

    private final ServicoService servicoService;

    @GetMapping
    public List<Servico> listarServicos(@AuthenticationPrincipal User loggedUser) {
        return servicoService.listarServicos(loggedUser);
    }

    @GetMapping("/{idServico}")
    public Servico listarUmServico(@AuthenticationPrincipal User loggedUser, Integer idServico) {
        return servicoService.listarUmServico(loggedUser, idServico);
    }

    @PostMapping
    public Servico cadastrarServico(@AuthenticationPrincipal User loggedUser, @RequestBody Servico servico) {
        return servicoService.cadastrarServico(loggedUser, servico);
    }

    @PutMapping("/{idServico}")
    public Servico editarServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico, @RequestBody Servico servico) {
        return servicoService.editarServico(loggedUser, idServico, servico);
    }

    @DeleteMapping("/{idServico}")
    public void excluirServico(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idServico) {
        servicoService.excluirServico(loggedUser, idServico);
    }
}
