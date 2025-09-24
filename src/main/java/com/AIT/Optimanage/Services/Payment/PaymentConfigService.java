package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfigService {

    private final PaymentConfigRepository repository;

    public PaymentConfig getConfig(Integer organizationId, PaymentProvider provider) {
        if (organizationId == null) {
            throw new MissingPaymentConfigurationException(provider);
        }
        return repository.findByOrganizationIdAndProvider(organizationId, provider)
                .orElseThrow(() -> new MissingPaymentConfigurationException(provider, organizationId));
    }

}
