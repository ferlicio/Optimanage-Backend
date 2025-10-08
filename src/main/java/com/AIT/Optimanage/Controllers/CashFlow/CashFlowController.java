package com.AIT.Optimanage.Controllers.CashFlow;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryRequest;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryResponse;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import com.AIT.Optimanage.Models.CashFlow.Search.CashFlowSearch;
import com.AIT.Optimanage.Services.CashFlow.CashFlowService;
import com.AIT.Optimanage.Support.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/cashflow")
@RequiredArgsConstructor
@Tag(name = "Fluxo de Caixa", description = "Operações de entradas e saídas financeiras")
public class CashFlowController extends V1BaseController {

    private final CashFlowService cashFlowService;

    @GetMapping
    @Operation(summary = "Listar lançamentos", description = "Retorna uma página de lançamentos do fluxo de caixa")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Page<CashFlowEntryResponse>> listarLancamentos(
            @RequestParam(value = "tipo", required = false) CashFlowType type,
            @RequestParam(value = "status", required = false) CashFlowStatus status,
            @RequestParam(value = "data_inicial", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(value = "data_final", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) Sort.Direction order,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pagesize", required = false) Integer legacyPageSize) {
        int resolvedPage = PaginationUtils.resolvePage(page);
        int resolvedPageSize = PaginationUtils.resolvePageSize(pageSize, legacyPageSize);
        CashFlowSearch search = CashFlowSearch.builder()
                .type(type)
                .status(status)
                .startDate(dataInicial)
                .endDate(dataFinal)
                .sort(sort)
                .order(order)
                .page(resolvedPage)
                .pageSize(resolvedPageSize)
                .build();
        return ok(cashFlowService.listarLancamentos(search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lançamento", description = "Retorna um lançamento do fluxo de caixa pelo ID")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<CashFlowEntryResponse> buscarLancamento(@PathVariable("id") Integer id) {
        return ok(cashFlowService.buscarLancamento(id));
    }

    @PostMapping
    @Operation(summary = "Criar lançamento", description = "Registra um novo lançamento no fluxo de caixa")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<CashFlowEntryResponse> criarLancamento(@RequestBody @Valid CashFlowEntryRequest request) {
        return created(cashFlowService.criarLancamento(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lançamento", description = "Atualiza um lançamento existente")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<?> atualizarLancamento(@PathVariable("id") Integer id,
                                                 @RequestBody @Valid CashFlowEntryRequest request) {
        try {
            return ok(cashFlowService.atualizarLancamento(id, request));
        } catch (IllegalStateException ex) {
            return badRequest(ex.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar lançamento", description = "Cancela um lançamento do fluxo de caixa")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<CashFlowEntryResponse> cancelarLancamento(@PathVariable("id") Integer id) {
        return ok(cashFlowService.cancelarLancamento(id));
    }
}
