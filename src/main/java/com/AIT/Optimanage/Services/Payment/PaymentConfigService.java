package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import com.AIT.Optimanage.Security.CurrentUser;
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

    public PaymentConfig getConfig(PaymentProvider provider) {
        Integer organizationId = CurrentUser.getOrganizationId();
        return getConfig(organizationId, provider);
    }
}
