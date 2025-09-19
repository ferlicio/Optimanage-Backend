package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Payment.PaymentConfigRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
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
    void shouldResolveOrganizationFromCurrentUser() {
        User user = new User();
        user.setOrganizationId(25);
        CurrentUser.set(user);

        PaymentConfig stored = PaymentConfig.builder()
                .provider(PaymentProvider.PAYPAL)
                .build();
        stored.setOrganizationId(25);

        when(repository.findByOrganizationIdAndProvider(25, PaymentProvider.PAYPAL))
                .thenReturn(Optional.of(stored));

        PaymentConfig result = service.getConfig(PaymentProvider.PAYPAL);

        assertSame(stored, result);
        verify(repository).findByOrganizationIdAndProvider(25, PaymentProvider.PAYPAL);
    }
}
