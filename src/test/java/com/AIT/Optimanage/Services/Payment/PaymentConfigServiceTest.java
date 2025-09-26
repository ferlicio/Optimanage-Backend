package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConfigServiceTest {

    @Mock
    private PaymentConfigRepository repository;

    private PaymentConfigService service;

    @BeforeEach
    void setUp() {
        service = new PaymentConfigService(repository);
    }

    @Test
    void shouldReturnPersistedConfigWhenAvailable() {
        PaymentConfig stored = PaymentConfig.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("persisted")
                .build();
        stored.setOrganizationId(10);

        when(repository.findByOrganizationIdAndProvider(10, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(stored));

        PaymentConfig result = service.getConfig(10, PaymentProvider.STRIPE);

        assertSame(stored, result);
    }

    @Test
    void shouldThrowWhenConfigMissing() {
        when(repository.findByOrganizationIdAndProvider(10, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        assertThrows(MissingPaymentConfigurationException.class,
                () -> service.getConfig(10, PaymentProvider.STRIPE));
    }

    @Test
    void shouldValidateOrganizationIdPresence() {
        assertThrows(MissingPaymentConfigurationException.class,
                () -> service.getConfig(null, PaymentProvider.PAYPAL));
    }
}
