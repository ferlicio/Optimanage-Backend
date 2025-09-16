package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.model.PaymentIntent;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentProviderStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        StripeClient client = new StripeClient(config.getApiKey());
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(request.getCurrency() == null ? "brl" : request.getCurrency())
                    .setDescription(request.getDescription())
                    .build();
            PaymentIntent intent = client.paymentIntents().create(params);
            return new PaymentResponseDTO(intent.getId(), intent.getClientSecret(), PaymentProvider.STRIPE);
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao criar pagamento", e);
        }
    }

    @Override
    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        StripeClient client = new StripeClient(config.getApiKey());
        try {
            PaymentIntent intent = client.paymentIntents().retrieve(paymentIntentId);
            if ("requires_confirmation".equals(intent.getStatus())) {
                intent = client.paymentIntents().confirm(paymentIntentId);
            }
            StatusPagamento status = "succeeded".equals(intent.getStatus()) ? StatusPagamento.PAGO : StatusPagamento.PENDENTE;
            BigDecimal amount = BigDecimal.valueOf(intent.getAmountReceived()).divide(BigDecimal.valueOf(100));
            return PagamentoDTO.builder()
                    .valorPago(amount)
                    .dataPagamento(LocalDate.now())
                    .dataVencimento(LocalDate.now())
                    .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                    .statusPagamento(status)
                    .observacoes("Stripe payment " + intent.getId())
                    .build();
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao confirmar pagamento", e);
        }
    }

    @Override
    public String extractWebhookEventId(String payload, Map<String, String> headers, PaymentConfig config) {
        return parseEvent(payload, headers, config).getId();
    }

    @Override
    public PagamentoDTO handleWebhook(String payload, Map<String, String> headers, PaymentConfig config) {
        Event event = parseEvent(payload, headers, config);
        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer data = event.getDataObjectDeserializer();
            PaymentIntent intent = (PaymentIntent) data.getObject().orElse(null);
            BigDecimal amount = intent != null && intent.getAmountReceived() != null ?
                    BigDecimal.valueOf(intent.getAmountReceived()).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
            return PagamentoDTO.builder()
                    .valorPago(amount)
                    .dataPagamento(LocalDate.now())
                    .dataVencimento(LocalDate.now())
                    .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                    .statusPagamento(StatusPagamento.PAGO)
                    .observacoes("Stripe webhook " + event.getId())
                    .build();
        }
        return PagamentoDTO.builder()
                .valorPago(BigDecimal.ZERO)
                .dataPagamento(LocalDate.now())
                .dataVencimento(LocalDate.now())
                .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                .statusPagamento(StatusPagamento.PENDENTE)
                .observacoes("Stripe webhook " + event.getId())
                .build();
    }

    private Event parseEvent(String payload, Map<String, String> headers, PaymentConfig config) {
        String signature = headers.getOrDefault("Stripe-Signature", "");
        try {
            return Webhook.constructEvent(payload, signature, config.getApiKey());
        } catch (Exception e) {
            throw new RuntimeException("Evento Stripe inválido", e);
        }
    }
}
