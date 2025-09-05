package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BoletoPaymentProvider implements PaymentProviderStrategy {
    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.BOLETO;
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        throw new UnsupportedOperationException("Boleto não implementado");
    }

    @Override
    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        throw new UnsupportedOperationException("Boleto não implementado");
    }
}
