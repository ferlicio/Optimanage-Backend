package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.Providers.PaymentProviderStrategy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final Map<PaymentProvider, PaymentProviderStrategy> providers;

    public PaymentService(List<PaymentProviderStrategy> strategies) {
        this.providers = new EnumMap<>(PaymentProvider.class);
        strategies.forEach(s -> this.providers.put(s.getProvider(), s));
    }

    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        return getProvider(request.getProvider()).createPayment(request, config);
    }

    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        return getProvider(config.getProvider()).confirmPayment(paymentIntentId, config);
    }

    public PagamentoDTO handleWebhook(PaymentProvider provider, String payload, Map<String, String> headers, PaymentConfig config) {
        return getProvider(provider).handleWebhook(payload, headers, config);
    }

    private PaymentProviderStrategy getProvider(PaymentProvider provider) {
        PaymentProviderStrategy strategy = providers.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Provedor de pagamento não suportado: " + provider);
        }
        return strategy;
    }
}
