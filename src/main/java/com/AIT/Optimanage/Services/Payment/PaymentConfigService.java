package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfigService {

    private final PaymentConfigRepository repository;

    public PaymentConfig getConfig(User user, PaymentProvider provider) {
        return repository.findByUserAndProvider(user, provider)
                .orElseThrow(() -> new MissingPaymentConfigurationException(provider));
    }

    public PaymentConfig getConfig(PaymentProvider provider) {
        return repository.findFirstByProvider(provider)
                .orElseThrow(() -> new MissingPaymentConfigurationException(provider));
    }
}
