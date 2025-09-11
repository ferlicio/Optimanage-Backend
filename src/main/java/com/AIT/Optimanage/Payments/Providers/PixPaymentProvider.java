package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PixPaymentProvider implements PaymentProviderStrategy {

    @Value("${pix.api.url}")
    private String apiUrl;

    @Value("${pix.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.PIX;
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        HttpHeaders headers = createHeaders(config);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);
        ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                apiUrl + "/payments",
                HttpMethod.POST,
                entity,
                PaymentResponseDTO.class);
        return response.getBody();
    }

    @Override
    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        HttpHeaders headers = createHeaders(config);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payments/" + paymentIntentId,
                HttpMethod.GET,
                entity,
                Map.class);
        Map body = response.getBody();
        String status = body != null ? (String) body.get("status") : "";
        BigDecimal amount = body != null && body.get("amount") != null
                ? new BigDecimal(body.get("amount").toString())
                : BigDecimal.ZERO;
        StatusPagamento statusPagamento = "PAID".equalsIgnoreCase(status)
                ? StatusPagamento.PAGO
                : StatusPagamento.PENDENTE;
        return PagamentoDTO.builder()
                .valorPago(amount)
                .dataPagamento(LocalDate.now())
                .formaPagamento(FormaPagamento.PIX)
                .statusPagamento(statusPagamento)
                .observacoes("PIX payment " + paymentIntentId)
                .build();
    }

    private HttpHeaders createHeaders(PaymentConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String key = config != null && config.getApiKey() != null && !config.getApiKey().isEmpty()
                ? config.getApiKey()
                : apiKey;
        if (key != null && !key.isEmpty()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + key);
        }
        return headers;
    }
}
