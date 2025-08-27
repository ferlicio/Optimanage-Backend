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
import jakarta.validation.Valid;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Operações relacionadas a compras")
public class CompraController extends V1BaseController {

    private final CompraService compraService;

    @GetMapping
    @Operation(summary = "Listar compras", description = "Retorna uma lista de compras")
    @ApiResponse(responseCode = "200", description = "Sucesso")
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
    @Operation(summary = "Listar compra", description = "Retorna uma compra pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra listarUmaCompra(@AuthenticationPrincipal User loggedUser,
                                    @PathVariable("idCompra") Integer idCompra) {
          return compraService.listarUmaCompra(loggedUser, idCompra);
      }

    @PostMapping
    @Operation(summary = "Criar compra", description = "Cria uma nova compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra criarCompra(@AuthenticationPrincipal User loggedUser,
                               @RequestBody @Valid CompraDTO compra) {
          return compraService.criarCompra(loggedUser, compra);
      }

    @PutMapping("/{idCompra}")
    @Operation(summary = "Editar compra", description = "Atualiza uma compra existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra editarCompra(@AuthenticationPrincipal User loggedUser,
                                 @PathVariable("idCompra") Integer idCompra,
                                 @RequestBody @Valid CompraDTO compra) {
          return compraService.editarCompra(loggedUser, idCompra, compra);
      }

    @PutMapping("/{idCompra}/confirmar")
    @Operation(summary = "Confirmar compra", description = "Confirma uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra confirmarCompra(@AuthenticationPrincipal User loggedUser,
                                   @PathVariable("idCompra") Integer idCompra) {
          return compraService.confirmarCompra(loggedUser, idCompra);
      }

    @PutMapping("/{idCompra}/pagar/{idPagamento}")
    @Operation(summary = "Pagar compra", description = "Realiza pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra pagarCompra(@AuthenticationPrincipal User loggedUser,
                                @PathVariable("idCompra") Integer idCompra,
                                @PathVariable("idPagamento") Integer idPagamento) {
          return compraService.pagarCompra(loggedUser, idCompra, idPagamento);
      }

    @PutMapping("/{idCompra}/lancar-pagamento")
    @Operation(summary = "Lançar pagamento", description = "Registra pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra lancarPagamentoCompra(@AuthenticationPrincipal User loggedUser,
                                          @PathVariable("idCompra") Integer idCompra,
                                          @RequestBody List<@Valid PagamentoDTO> pagamentoDTO) {
          return compraService.lancarPagamentoCompra(loggedUser, idCompra, pagamentoDTO);
      }

    @PutMapping("/{idCompra}/estornar")
    @Operation(summary = "Estornar compra", description = "Estorna uma compra integralmente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra estornarCompraIntegral(@AuthenticationPrincipal User loggedUser,
                                           @PathVariable("idCompra") Integer idCompra) {
          return compraService.estornarCompraIntegral(loggedUser, idCompra);
      }

    @PutMapping("/{idCompra}/estornar/{idPagamento}")
    @Operation(summary = "Estornar pagamento", description = "Estorna pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra estornarPagamentoCompra(@AuthenticationPrincipal User loggedUser,
                                            @PathVariable("idCompra") Integer idCompra,
                                            @PathVariable("idPagamento") Integer idPagamento) {
          return compraService.estornarPagamentoCompra(loggedUser, idCompra, idPagamento);
      }

    @PutMapping("/{idCompra}/finalizar")
    @Operation(summary = "Finalizar compra", description = "Finaliza uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra finalizarCompra(@AuthenticationPrincipal User loggedUser,
                                   @PathVariable("idCompra") Integer idCompra) {
          return compraService.finalizarCompra(loggedUser, idCompra);
      }

    @PutMapping("/{idCompra}/cancelar")
    @Operation(summary = "Cancelar compra", description = "Cancela uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public Compra cancelarCompra(@AuthenticationPrincipal User loggedUser,
                                   @PathVariable("idCompra") Integer idCompra) {
          return compraService.cancelarCompra(loggedUser, idCompra);
      }
}
