package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.Payment.ProcessedPaymentEvent;
import com.AIT.Optimanage.Payments.Providers.PaymentProviderStrategy;
import com.AIT.Optimanage.Repositories.Payment.ProcessedPaymentEventRepository;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final Map<PaymentProvider, PaymentProviderStrategy> providers;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    public PaymentService(List<PaymentProviderStrategy> strategies,
                          ProcessedPaymentEventRepository processedPaymentEventRepository) {
        this.providers = new EnumMap<>(PaymentProvider.class);
        strategies.forEach(s -> this.providers.put(s.getProvider(), s));
        this.processedPaymentEventRepository = processedPaymentEventRepository;
    }

    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        return getProvider(request.getProvider()).createPayment(request, config);
    }

    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        return getProvider(config.getProvider()).confirmPayment(paymentIntentId, config);
    }

    @Transactional
    public PagamentoDTO handleWebhook(PaymentProvider provider, String payload, Map<String, String> headers, PaymentConfig config) {
        PaymentProviderStrategy strategy = getProvider(provider);
        String eventId = strategy.extractWebhookEventId(payload, headers, config);
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("ID do evento inválido");
        }

        Integer organizationId = config.getOrganizationId();
        if (organizationId == null) {
            throw new IllegalStateException("Configuração de pagamento sem organização associada");
        }
        ProcessedPaymentEvent event = new ProcessedPaymentEvent();
        event.setEventId(eventId);
        event.setProvider(provider);
        event.setProcessedAt(LocalDateTime.now());
        event.setOrganizationId(organizationId);
        try {
            processedPaymentEventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ignored) {
            // Se outro processo registrou o evento simultaneamente, ignoramos o erro.
            return null;
        }

        PagamentoDTO result = strategy.handleWebhook(payload, headers, config);

        return result;
    }

    private PaymentProviderStrategy getProvider(PaymentProvider provider) {
        PaymentProviderStrategy strategy = providers.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Provedor de pagamento não suportado: " + provider);
        }
        return strategy;
    }
}
