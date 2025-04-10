package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Search.VendaSearch;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Services.Venda.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController extends V1BaseController {

    private final VendaService vendaService;

    @GetMapping
    public List<Venda> listarVendas(@AuthenticationPrincipal User loggedUser,
                                    @RequestParam(value = "id", required = false) Integer id,
                                    @RequestParam(value = "cliente_id", required = false) Integer clienteId,
                                    @RequestParam(value = "data_inicial", required = false) String data_inicial,
                                    @RequestParam(value = "data_final", required = false) String data_final,
                                    @RequestParam(value = "pago", required = false) Boolean pago,
                                    @RequestParam(value = "status", required = false) StatusVenda status,
                                    @RequestParam(value = "forma_pagamento", required = false) FormaPagamento forma_pagamento,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    @RequestParam(value = "order", required = false) Sort.Direction order,
                                    @RequestParam(value = "page", required = true) Integer page,
                                    @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        var pesquisa = VendaSearch.builder()
                .id(id)
                .clienteId(clienteId)
                .dataInicial(data_inicial)
                .dataFinal(data_final)
                .formaPagamento(forma_pagamento)
                .pago(pago)
                .status(status)
                .page(page)
                .pageSize(pagesize)
                .sort(sort)
                .order(order)
                .build();
        return vendaService.listarVendas(loggedUser, pesquisa);
    }



    @GetMapping("/{idVenda}")
    public Venda listarUmaVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.listarUmaVenda(loggedUser, idVenda);
    }

    @PostMapping
    public Venda registrarVenda(@AuthenticationPrincipal User loggedUser, VendaDTO venda) {
        return vendaService.registrarVenda(loggedUser, venda);
    }

    @PutMapping("/{idVenda}")
    public Venda editarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda, VendaDTO venda) {
        return vendaService.atualizarVenda(loggedUser, idVenda, venda);
    }

    @GetMapping("/{idVenda}/confirmar")
    public Venda confirmarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.confirmarVenda(loggedUser, idVenda);
    }

    @PutMapping("/{idVenda}/pagar/{idPagamento}")
    public Venda pagarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda, Integer idPagamento) {
        return vendaService.pagarVenda(loggedUser, idVenda, idPagamento);
    }

    @PutMapping("/{idVenda}/lancar-pagamento")
    public Venda lancarPagamentoVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda, List<PagamentoDTO> pagamentoDTO) {
        return vendaService.lancarPagamentoVenda(loggedUser, idVenda, pagamentoDTO);
    }

    @GetMapping("/{idVenda}/estornar")
    public Venda estornarVendaIntegral(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.estornarVendaIntegral(loggedUser, idVenda);
    }

    @GetMapping("/{idVenda}/estornar/{idPagamento}")
    public Venda estornarPagamentoVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda, Integer idPagamento) {
        return vendaService.estornarPagamentoVenda(loggedUser, idVenda, idPagamento);
    }

    @PutMapping("/{idVenda}/agendar")
    public Venda agendarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda, String dataAgendada) {
        return vendaService.agendarVenda(loggedUser, idVenda, dataAgendada);
    }

    @GetMapping("/{idVenda}/finalizar-agendamento")
    public Venda finalizarAgendamentoVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.finalizarAgendamentoVenda(loggedUser, idVenda);
    }

    @GetMapping("/{idVenda}/finalizar")
    public Venda finalizarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.finalizarVenda(loggedUser, idVenda);
    }

    @GetMapping("/{idVenda}/cancelar")
    public Venda cancelarVenda(@AuthenticationPrincipal User loggedUser, Integer idVenda) {
        return vendaService.cancelarVenda(loggedUser, idVenda);
    }

}
