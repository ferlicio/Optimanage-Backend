package com.AIT.Optimanage.Controllers.Compra;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Compra.Search.CompraSearch;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Services.Compra.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController extends V1BaseController {

    private final CompraService compraService;

    @GetMapping
    public List<Compra> listarCompras(@AuthenticationPrincipal User loggedUser,
                                      @RequestParam(value = "id", required = false) Integer id,
                                      @RequestParam(value = "fornecedor_id", required = false) Integer fornecedorId,
                                      @RequestParam(value = "data_inicial", required = false) String data_inicial,
                                      @RequestParam(value = "data_final", required = false) String data_final,
                                      @RequestParam(value = "pago", required = false) Boolean pago,
                                      @RequestParam(value = "status", required = false) StatusCompra status,
                                      @RequestParam(value = "forma_pagamento", required = false) FormaPagamento forma_pagamento,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "order", required = false) Sort.Direction order,
                                      @RequestParam(value = "page", required = true) Integer page,
                                      @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        CompraSearch pesquisa = CompraSearch.builder()
                .id(id)
                .fornecedorId(fornecedorId)
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
        return compraService.listarCompras(loggedUser, pesquisa);
    }

    @GetMapping("/{idCompra}")
    public Compra listarUmaCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra) {
        return compraService.listarUmaCompra(loggedUser, idCompra);
    }

    @PostMapping
    public Compra criarCompra(@AuthenticationPrincipal User loggedUser, @RequestBody CompraDTO compra) {
        return compraService.criarCompra(loggedUser, compra);
    }

    @PutMapping("/{idCompra}")
    public Compra editarCompra(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idCompra, @RequestBody CompraDTO compra) {
        return compraService.editarCompra(loggedUser, idCompra, compra);
    }

    @PutMapping("/{idCompra}/confirmar")
    public Compra confirmarCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra) {
        return compraService.confirmarCompra(loggedUser, idCompra);
    }

    @PutMapping("/{idCompra}/pagar/{idPagamento}")
    public Compra pagarCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra, Integer idPagamento) {
        return compraService.pagarCompra(loggedUser, idCompra, idPagamento);
    }

    @PutMapping("/{idCompra}/lancar-pagamento")
    public Compra lancarPagamentoCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra, @RequestBody List<PagamentoDTO> pagamentoDTO) {
        return compraService.lancarPagamentoCompra(loggedUser, idCompra, pagamentoDTO);
    }

    @PutMapping("/{idCompra}/estornar")
    public Compra estornarCompraIntegral(@AuthenticationPrincipal User loggedUser, Integer idCompra) {
        return compraService.estornarCompraIntegral(loggedUser, idCompra);
    }

    @PutMapping("/{idCompra}/estornar/{idPagamento}")
    public Compra estornarPagamentoCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra, Integer idPagamento) {
        return compraService.estornarPagamentoCompra(loggedUser, idCompra, idPagamento);
    }

    @PutMapping("/{idCompra}/finalizar")
    public Compra finalizarCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra) {
        return compraService.finalizarCompra(loggedUser, idCompra);
    }

    @PutMapping("/{idCompra}/cancelar")
    public Compra cancelarCompra(@AuthenticationPrincipal User loggedUser, Integer idCompra) {
        return compraService.cancelarCompra(loggedUser, idCompra);
    }
}
