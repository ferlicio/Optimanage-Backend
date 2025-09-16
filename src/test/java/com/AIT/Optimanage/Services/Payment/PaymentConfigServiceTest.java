package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        User user = new User();
        user.setOrganizationId(10);
        PaymentConfig stored = PaymentConfig.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("persisted")
                .user(user)
                .build();
        stored.setOrganizationId(10);

        when(repository.findByUserAndProvider(user, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(stored));

        PaymentConfig result = service.getConfig(user, PaymentProvider.STRIPE);

        assertSame(stored, result);
    }

    @Test
    void shouldFallbackToDefaultStripeCredentialsWhenMissing() {
        User user = new User();
        user.setOrganizationId(42);

        when(repository.findByUserAndProvider(user, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        ReflectionTestUtils.setField(service, "defaultStripeApiKey", "sk_test_default");

        PaymentConfig config = service.getConfig(user, PaymentProvider.STRIPE);

        assertNotNull(config);
        assertEquals(PaymentProvider.STRIPE, config.getProvider());
        assertEquals("sk_test_default", config.getApiKey());
        assertEquals(user.getOrganizationId(), config.getOrganizationId());
        assertSame(user, config.getUser());
    }

    @Test
    void shouldThrowWhenConfigMissingAndNoFallbackAvailable() {
        User user = new User();

        when(repository.findByUserAndProvider(user, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        ReflectionTestUtils.setField(service, "defaultStripeApiKey", "");

        assertThrows(MissingPaymentConfigurationException.class,
                () -> service.getConfig(user, PaymentProvider.STRIPE));
    }
}
