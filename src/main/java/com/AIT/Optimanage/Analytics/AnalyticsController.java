package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.InventoryAlertDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformResumoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController extends V1BaseController {

    private final AnalyticsService analyticsService;

    @GetMapping("/resumo")
    public ResponseEntity<ResumoDTO> resumo() {
        return ok(analyticsService.obterResumo());
    }

    @GetMapping("/previsao")
    public ResponseEntity<PrevisaoDTO> previsao() {
        return ok(analyticsService.preverDemanda());
    }

    @GetMapping("/estoque-critico")
    public ResponseEntity<List<InventoryAlertDTO>> estoqueCritico() {
        return ok(analyticsService.listarAlertasEstoque());
    }

    @GetMapping("/plataforma/resumo")
    public ResponseEntity<PlatformResumoDTO> resumoPlataforma() {
        analyticsService.requirePlatformOrganization();
        return ok(analyticsService.obterResumoPlataforma());
    }
}

