package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaResponseDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Search.VendaSearch;
import com.AIT.Optimanage.Services.RecommendationService;
import com.AIT.Optimanage.Services.Venda.VendaService;
import com.AIT.Optimanage.Payments.PaymentConfirmationDTO;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Operações relacionadas a vendas")
public class VendaController extends V1BaseController {

    private final VendaService vendaService;
    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Listar vendas", description = "Retorna uma página de vendas")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<VendaResponseDTO>> listarVendas(@AuthenticationPrincipal User loggedUser,
                                                    @RequestParam(value = "id", required = false) Integer id,
                                                    @RequestParam(value = "cliente_id", required = false) Integer clienteId,
                                                    @RequestParam(value = "data_inicial", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_inicial,
                                                    @RequestParam(value = "data_final", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_final,
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
        return ok(vendaService.listarVendas(loggedUser, pesquisa));
    }

    @GetMapping("/{idVenda}")
    @Operation(summary = "Listar venda", description = "Retorna uma venda pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> listarUmaVenda(@AuthenticationPrincipal User loggedUser,
                                                @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.listarUmaVenda(loggedUser, idVenda));
    }

    @PostMapping
    @Operation(summary = "Registrar venda", description = "Cria uma nova venda")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<VendaResponseDTO> registrarVenda(@AuthenticationPrincipal User loggedUser,
                                                @RequestBody @Valid VendaDTO venda) {
        return created(vendaService.registrarVenda(loggedUser, venda));
    }

    @PutMapping("/{idVenda}")
    @Operation(summary = "Editar venda", description = "Atualiza uma venda existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> editarVenda(@AuthenticationPrincipal User loggedUser,
                                             @PathVariable("idVenda") Integer idVenda,
                                             @RequestBody @Valid VendaDTO venda) {
        return ok(vendaService.atualizarVenda(loggedUser, idVenda, venda));
    }

    @PutMapping("/{idVenda}/confirmar")
    @Operation(summary = "Confirmar venda", description = "Confirma uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> confirmarVenda(@AuthenticationPrincipal User loggedUser,
                                                @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.confirmarVenda(loggedUser, idVenda));
    }

    @PutMapping("/{idVenda}/pagar/{idPagamento}")
    @Operation(summary = "Pagar venda", description = "Realiza pagamento de uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> pagarVenda(@AuthenticationPrincipal User loggedUser,
                                            @PathVariable("idVenda") Integer idVenda,
                                            @PathVariable("idPagamento") Integer idPagamento) {
        return ok(vendaService.pagarVenda(loggedUser, idVenda, idPagamento));
    }

    @PostMapping("/{idVenda}/pagamento-externo")
    @Operation(summary = "Iniciar pagamento externo", description = "Cria cobrança em provedor externo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<PaymentResponseDTO> iniciarPagamentoExterno(@AuthenticationPrincipal User loggedUser,
                                                                      @PathVariable("idVenda") Integer idVenda,
                                                                      @RequestBody(required = false) @Valid PaymentRequestDTO request) {
        return ok(vendaService.iniciarPagamentoExterno(loggedUser, idVenda, request));
    }

    @PostMapping("/{idVenda}/pagamento-externo/confirmar")
    @Operation(summary = "Confirmar pagamento externo", description = "Confirma pagamento no provedor externo")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> confirmarPagamentoExterno(@AuthenticationPrincipal User loggedUser,
                                                           @PathVariable("idVenda") Integer idVenda,
                                                           @RequestBody @Valid PaymentConfirmationDTO confirmDTO) {
        return ok(vendaService.confirmarPagamentoExterno(loggedUser, idVenda, confirmDTO));
    }

    @PutMapping("/{idVenda}/lancar-pagamento")
    @Operation(summary = "Lançar pagamento", description = "Registra pagamento de uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> lancarPagamentoVenda(@AuthenticationPrincipal User loggedUser,
                                                      @PathVariable("idVenda") Integer idVenda,
                                                      @RequestBody List<@Valid PagamentoDTO> pagamentoDTO) {
        return ok(vendaService.lancarPagamentoVenda(loggedUser, idVenda, pagamentoDTO));
    }

    @PutMapping("/{idVenda}/estornar")
    @Operation(summary = "Estornar venda", description = "Estorna venda integralmente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> estornarVendaIntegral(@AuthenticationPrincipal User loggedUser,
                                                       @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.estornarVendaIntegral(loggedUser, idVenda));
    }

    @PutMapping("/{idVenda}/estornar/{idPagamento}")
    @Operation(summary = "Estornar pagamento", description = "Estorna pagamento de uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> estornarPagamentoVenda(@AuthenticationPrincipal User loggedUser,
                                                        @PathVariable("idVenda") Integer idVenda,
                                                        @PathVariable("idPagamento") Integer idPagamento) {
        return ok(vendaService.estornarPagamentoVenda(loggedUser, idVenda, idPagamento));
    }

    @PutMapping("/{idVenda}/agendar")
    @Operation(summary = "Agendar venda", description = "Agenda uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<?> agendarVenda(@AuthenticationPrincipal User loggedUser,
                                          @PathVariable("idVenda") Integer idVenda,
                                          @RequestParam String dataAgendada,
                                          @RequestParam String horaAgendada,
                                          @RequestParam(value = "duracaoMinutos", required = false) Integer duracaoMinutos) {
        try {
            return ok(vendaService.agendarVenda(loggedUser, idVenda, dataAgendada, horaAgendada, duracaoMinutos));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return badRequest(ex.getMessage());
        }
    }

    @PutMapping("/{idVenda}/finalizar-agendamento")
    @Operation(summary = "Finalizar agendamento", description = "Finaliza o agendamento de uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> finalizarAgendamentoVenda(@AuthenticationPrincipal User loggedUser,
                                                           @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.finalizarAgendamentoVenda(loggedUser, idVenda));
    }

    @PutMapping("/{idVenda}/finalizar")
    @Operation(summary = "Finalizar venda", description = "Finaliza uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> finalizarVenda(@AuthenticationPrincipal User loggedUser,
                                                @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.finalizarVenda(loggedUser, idVenda));
    }

    @PutMapping("/{idVenda}/cancelar")
    @Operation(summary = "Cancelar venda", description = "Cancela uma venda")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<VendaResponseDTO> cancelarVenda(@AuthenticationPrincipal User loggedUser,
                                               @PathVariable("idVenda") Integer idVenda) {
        return ok(vendaService.cancelarVenda(loggedUser, idVenda));
    }

    @GetMapping("/recomendacoes")
    @Operation(summary = "Recomendar produtos",
            description = "Sugere itens com base no histórico de vendas, priorizando recorrência, margem e compatibilidade")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<List<ProdutoResponse>> recomendarProdutos(@AuthenticationPrincipal User loggedUser,
                                                                    @RequestParam(value = "clienteId", required = false) Integer clienteId,
                                                                    @RequestParam(value = "contexto", required = false) String contexto,
                                                                    @RequestParam(value = "estoquePositivo", required = false) Boolean estoquePositivo) {
        return ok(recommendationService.recomendarProdutos(clienteId, contexto, estoquePositivo));
    }
}

