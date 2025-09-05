package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        Stripe.apiKey = stripeApiKey;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(request.getCurrency() == null ? "brl" : request.getCurrency())
                    .setDescription(request.getDescription())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentResponseDTO(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao criar pagamento", e);
        }
    }

    public PagamentoDTO confirmPayment(String paymentIntentId) {
        Stripe.apiKey = stripeApiKey;
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            if ("requires_confirmation".equals(intent.getStatus())) {
                intent = intent.confirm();
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
