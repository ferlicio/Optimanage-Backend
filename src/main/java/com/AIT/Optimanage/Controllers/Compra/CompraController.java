package com.AIT.Optimanage.Controllers.Compra;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraResponseDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Compra.Search.CompraSearch;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Services.Compra.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.data.domain.Page;
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
    @Operation(summary = "Listar compras", description = "Retorna uma página de compras")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<CompraResponseDTO>> listarCompras(@RequestParam(value = "id", required = false) Integer id,
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
        return ok(compraService.listarCompras(pesquisa));
    }

    @GetMapping("/{idCompra}")
    @Operation(summary = "Listar compra", description = "Retorna uma compra pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> listarUmaCompra(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.listarUmaCompra(idCompra));
      }

    @PostMapping
    @Operation(summary = "Criar compra", description = "Cria uma nova compra")
    @ApiResponse(responseCode = "201", description = "Criado")
      public ResponseEntity<CompraResponseDTO> criarCompra(@RequestBody @Valid CompraDTO compra) {
          return created(compraService.criarCompra(compra));
      }

    @PutMapping("/{idCompra}")
    @Operation(summary = "Editar compra", description = "Atualiza uma compra existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> editarCompra(@PathVariable("idCompra") Integer idCompra,
                                                 @RequestBody @Valid CompraDTO compra) {
          return ok(compraService.editarCompra(idCompra, compra));
      }

    @PutMapping("/{idCompra}/confirmar")
    @Operation(summary = "Confirmar compra", description = "Confirma uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> confirmarCompra(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.confirmarCompra(idCompra));
      }

    @PutMapping("/{idCompra}/pagar/{idPagamento}")
    @Operation(summary = "Pagar compra", description = "Realiza pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> pagarCompra(@PathVariable("idCompra") Integer idCompra,
                                                @PathVariable("idPagamento") Integer idPagamento) {
          return ok(compraService.pagarCompra(idCompra, idPagamento));
      }

    @PutMapping("/{idCompra}/lancar-pagamento")
    @Operation(summary = "Lançar pagamento", description = "Registra pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> lancarPagamentoCompra(@PathVariable("idCompra") Integer idCompra,
                                                          @RequestBody List<@Valid PagamentoDTO> pagamentoDTO) {
          return ok(compraService.lancarPagamentoCompra(idCompra, pagamentoDTO));
      }

    @PutMapping("/{idCompra}/estornar")
    @Operation(summary = "Estornar compra", description = "Estorna uma compra integralmente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> estornarCompraIntegral(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.estornarCompraIntegral(idCompra));
      }

    @PutMapping("/{idCompra}/estornar/{idPagamento}")
    @Operation(summary = "Estornar pagamento", description = "Estorna pagamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> estornarPagamentoCompra(@PathVariable("idCompra") Integer idCompra,
                                                            @PathVariable("idPagamento") Integer idPagamento) {
          return ok(compraService.estornarPagamentoCompra(idCompra, idPagamento));
      }

    @PutMapping("/{idCompra}/agendar")
    @Operation(summary = "Agendar compra", description = "Agenda uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> agendarCompra(@PathVariable("idCompra") Integer idCompra,
                                                 @RequestParam String dataAgendada) {
          return ok(compraService.agendarCompra(idCompra, dataAgendada));
      }

    @PutMapping("/{idCompra}/finalizar-agendamento")
    @Operation(summary = "Finalizar agendamento", description = "Finaliza o agendamento de uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> finalizarAgendamentoCompra(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.finalizarAgendamentoCompra(idCompra));
      }

    @PutMapping("/{idCompra}/finalizar")
    @Operation(summary = "Finalizar compra", description = "Finaliza uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> finalizarCompra(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.finalizarCompra(idCompra));
      }

    @PutMapping("/{idCompra}/cancelar")
    @Operation(summary = "Cancelar compra", description = "Cancela uma compra")
    @ApiResponse(responseCode = "200", description = "Sucesso")
      public ResponseEntity<CompraResponseDTO> cancelarCompra(@PathVariable("idCompra") Integer idCompra) {
          return ok(compraService.cancelarCompra(idCompra));
      }
}
