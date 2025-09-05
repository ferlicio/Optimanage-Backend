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
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

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
                    .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                    .statusPagamento(status)
                    .observacoes("Stripe payment " + intent.getId())
                    .build();
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao confirmar pagamento", e);
        }
    }
}
