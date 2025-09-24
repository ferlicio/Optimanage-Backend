package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentService;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Recepção de webhooks de pagamentos")
public class PaymentsController extends V1BaseController {

    private final PaymentService paymentService;
    private final PaymentConfigService paymentConfigService;

    @PostMapping("/webhook")
    @Operation(summary = "Webhook de pagamento", description = "Confirma pagamentos assíncronos")
    @ApiResponse(responseCode = "204", description = "Processado com sucesso")
    public ResponseEntity<Void> handleWebhook(@RequestParam("provider") PaymentProvider provider,
                                              @RequestBody String payload,
                                              @RequestHeader Map<String, String> headers) {
        PaymentConfig config = paymentConfigService.getConfig(provider);
        paymentService.handleWebhook(provider, payload, headers, config);
        return noContent();
    }
}
