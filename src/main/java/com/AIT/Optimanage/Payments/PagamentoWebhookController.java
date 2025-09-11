package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagamentos")
@RequiredArgsConstructor
public class PagamentoWebhookController {

    private final PaymentService paymentService;
    private final PaymentConfigService paymentConfigService;

    @PostMapping("/webhook")
    public ResponseEntity<PagamentoDTO> handleWebhook(@RequestParam PaymentProvider provider,
                                                      @RequestBody String payload,
                                                      @RequestHeader Map<String, String> headers) {
        PaymentConfig config = paymentConfigService.getConfig(provider);
        PagamentoDTO dto = paymentService.handleWebhook(provider, payload, headers, config);
        return ResponseEntity.ok(dto);
    }
}
