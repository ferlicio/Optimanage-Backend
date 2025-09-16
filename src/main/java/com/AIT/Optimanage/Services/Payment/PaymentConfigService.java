package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentConfigService {

    private final PaymentConfigRepository repository;

    @Value("${stripe.api.key:}")
    private String defaultStripeApiKey;

    @Value("${boleto.api.key:}")
    private String defaultBoletoApiKey;

    @Value("${pix.api.key:}")
    private String defaultPixApiKey;

    @Value("${paypal.client.id:}")
    private String defaultPaypalClientId;

    @Value("${paypal.client.secret:}")
    private String defaultPaypalClientSecret;

    @Value("${paypal.environment:sandbox}")
    private String defaultPaypalEnvironment;

    public PaymentConfig getConfig(User user, PaymentProvider provider) {
        return repository.findByUserAndProvider(user, provider)
                .or(() -> fallbackConfig(user, provider))
                .orElseThrow(() -> new MissingPaymentConfigurationException(provider));
    }

    public PaymentConfig getConfig(PaymentProvider provider) {
        return repository.findFirstByProvider(provider)
                .orElseThrow(() -> new MissingPaymentConfigurationException(provider));
    }

    private Optional<PaymentConfig> fallbackConfig(User user, PaymentProvider provider) {
        if (user == null) {
            return Optional.empty();
        }

        PaymentConfig.PaymentConfigBuilder builder = PaymentConfig.builder()
                .provider(provider)
                .user(user);

        switch (provider) {
            case STRIPE -> {
                if (!StringUtils.hasText(defaultStripeApiKey)) {
                    return Optional.empty();
                }
                builder.apiKey(defaultStripeApiKey);
            }
            case BOLETO -> {
                if (!StringUtils.hasText(defaultBoletoApiKey)) {
                    return Optional.empty();
                }
                builder.apiKey(defaultBoletoApiKey);
            }
            case PIX -> {
                if (!StringUtils.hasText(defaultPixApiKey)) {
                    return Optional.empty();
                }
                builder.apiKey(defaultPixApiKey);
            }
            case PAYPAL -> {
                if (!StringUtils.hasText(defaultPaypalClientId) || !StringUtils.hasText(defaultPaypalClientSecret)) {
                    return Optional.empty();
                }
                builder.clientId(defaultPaypalClientId)
                        .clientSecret(defaultPaypalClientSecret)
                        .environment(StringUtils.hasText(defaultPaypalEnvironment) ? defaultPaypalEnvironment : null);
            }
            default -> {
                return Optional.empty();
            }
        }

        PaymentConfig fallback = builder.build();
        if (user.getOrganizationId() != null) {
            fallback.setOrganizationId(user.getOrganizationId());
        }
        return Optional.of(fallback);
    }
}
