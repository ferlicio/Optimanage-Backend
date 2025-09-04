package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Services.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recomendações", description = "Sugestões de produtos baseadas em vendas")
public class RecommendationController extends V1BaseController {

    private final RecommendationService recommendationService;

    @GetMapping("/{clienteId}")
    @Operation(summary = "Recomendar produtos", description = "Sugere produtos com base no histórico de vendas")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<List<ProdutoResponse>> recomendar(@PathVariable Integer clienteId) {
        return ok(recommendationService.recomendarProdutos(clienteId));
    }
}
