package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import java.util.Map;

public interface PaymentProviderStrategy {
    PaymentProvider getProvider();
    PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config);
    PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config);
    default String extractWebhookEventId(String payload, Map<String, String> headers, PaymentConfig config) {
        throw new UnsupportedOperationException("Webhook not supported");
    }

    default PagamentoDTO handleWebhook(String payload, Map<String, String> headers, PaymentConfig config) {
        throw new UnsupportedOperationException("Webhook not supported");
    }
}
