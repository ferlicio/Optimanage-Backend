package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController extends V1BaseController {

    private final AnalyticsService analyticsService;

    @GetMapping("/resumo")
    public ResponseEntity<ResumoDTO> resumo(@AuthenticationPrincipal User user) {
        return ok(analyticsService.obterResumo(user));
    }

    @GetMapping("/previsao")
    public ResponseEntity<PrevisaoDTO> previsao(@AuthenticationPrincipal User user) {
        return ok(analyticsService.preverDemanda(user));
    }
}

