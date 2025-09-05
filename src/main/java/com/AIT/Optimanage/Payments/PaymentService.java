package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.Providers.PaymentProviderStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<PaymentProvider, PaymentProviderStrategy> providers;

    public PaymentService(List<PaymentProviderStrategy> strategies) {
        this.providers = strategies.stream()
                .collect(Collectors.toMap(PaymentProviderStrategy::getProvider, Function.identity()));
    }

    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        return getProvider(request.getProvider()).createPayment(request, config);
    }

    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        return getProvider(config.getProvider()).confirmPayment(paymentIntentId, config);
    }

    private PaymentProviderStrategy getProvider(PaymentProvider provider) {
        PaymentProviderStrategy strategy = providers.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Provedor de pagamento não suportado: " + provider);
        }
        return strategy;
    }
}
