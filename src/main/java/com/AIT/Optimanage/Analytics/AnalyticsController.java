package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.InventoryAlertDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformEngajamentoDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformFeatureAdoptionDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformHealthScoreDTO;
import com.AIT.Optimanage.Analytics.DTOs.PlatformOrganizationsResumoDTO;
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

    @GetMapping({"/plataforma/organizacoes/visao-geral", "/plataforma/organizacoes/resumo"})
    public ResponseEntity<PlatformOrganizationsResumoDTO> resumoOrganizacoesPlataforma() {
        return ok(analyticsService.obterResumoPlataforma());
    }

    @GetMapping("/plataforma/organizacoes/health-score")
    public ResponseEntity<PlatformHealthScoreDTO> healthScoreOrganizacoesPlataforma() {
        return ok(analyticsService.obterHealthScorePlataforma());
    }

    @GetMapping({"/plataforma/resumo", "/plataforma/resumo-financeiro"})
    public ResponseEntity<PlatformResumoDTO> resumoFinanceiroPlataforma() {
        return ok(analyticsService.obterResumoFinanceiroPlataforma());
    }

    @GetMapping("/plataforma/engajamento")
    public ResponseEntity<PlatformEngajamentoDTO> engajamentoPlataforma() {
        return ok(analyticsService.obterEngajamentoPlataforma());
    }

    @GetMapping("/plataforma/adocao-recursos")
    public ResponseEntity<PlatformFeatureAdoptionDTO> adocaoRecursosPlataforma() {
        return ok(analyticsService.obterAdocaoRecursosPlataforma());
    }

}

