package com.AIT.Optimanage.Controllers.Marketplace;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.MarketplaceConnectionRequest;
import com.AIT.Optimanage.Controllers.dto.MarketplaceIntegrationResponse;
import com.AIT.Optimanage.Controllers.dto.MarketplaceSyncResponse;
import com.AIT.Optimanage.Services.Marketplace.MarketplaceIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marketplace/integracao")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Integração com marketplaces externos")
public class MarketplaceIntegrationController extends V1BaseController {

    private final MarketplaceIntegrationService integrationService;

    @PostMapping
    @Operation(summary = "Conectar marketplace", description = "Configura a integração com um marketplace")
    @ApiResponse(responseCode = "200", description = "Integração configurada com sucesso")
    public ResponseEntity<MarketplaceIntegrationResponse> conectar(
            @RequestBody @Valid MarketplaceConnectionRequest request) {
        return ok(integrationService.conectar(request));
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar marketplace", description = "Executa a sincronização com o marketplace configurado")
    @ApiResponse(responseCode = "200", description = "Sincronização executada")
    public ResponseEntity<MarketplaceSyncResponse> sincronizar() {
        return ok(integrationService.sincronizar());
    }

    @GetMapping
    @Operation(summary = "Consultar integração", description = "Retorna o status atual da integração com marketplace")
    @ApiResponse(responseCode = "200", description = "Status retornado")
    public ResponseEntity<MarketplaceIntegrationResponse> obterIntegracao() {
        MarketplaceIntegrationResponse response = integrationService.obterStatusAtual();
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ok(response);
    }
}
