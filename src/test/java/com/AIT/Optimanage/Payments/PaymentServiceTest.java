package com.AIT.Optimanage.Payments;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.Providers.PaymentProviderStrategy;
import com.AIT.Optimanage.Repositories.Payment.ProcessedPaymentEventRepository;
import java.util.List;
import java.util.Map;
import org.mockito.InOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentProviderStrategy providerStrategy;

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        when(providerStrategy.getProvider()).thenReturn(PaymentProvider.STRIPE);
        paymentService = new PaymentService(List.of(providerStrategy), processedPaymentEventRepository);
    }

    @Test
    void shouldProcessWebhookWhenEventIsNew() {
        PaymentConfig config = PaymentConfig.builder().provider(PaymentProvider.STRIPE).build();
        config.setOrganizationId(1);
        PagamentoDTO dto = PagamentoDTO.builder().observacoes("processed").build();

        when(providerStrategy.extractWebhookEventId(anyString(), anyMap(), same(config))).thenReturn("evt_123");
        when(processedPaymentEventRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(providerStrategy.handleWebhook(anyString(), anyMap(), same(config))).thenReturn(dto);

        PagamentoDTO result = paymentService.handleWebhook(PaymentProvider.STRIPE, "{}", Map.of(), config);

        assertSame(dto, result);
        InOrder inOrder = inOrder(processedPaymentEventRepository, providerStrategy);
        inOrder.verify(processedPaymentEventRepository).saveAndFlush(any());
        inOrder.verify(providerStrategy).handleWebhook(anyString(), anyMap(), same(config));
    }

    @Test
    void shouldIgnoreWebhookWhenEventWasAlreadyProcessed() {
        PaymentConfig config = PaymentConfig.builder().provider(PaymentProvider.STRIPE).build();
        config.setOrganizationId(1);

        when(providerStrategy.extractWebhookEventId(anyString(), anyMap(), same(config))).thenReturn("evt_123");
        when(processedPaymentEventRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        PagamentoDTO result = paymentService.handleWebhook(PaymentProvider.STRIPE, "{}", Map.of(), config);

        assertNull(result);
        verify(providerStrategy, never()).handleWebhook(anyString(), anyMap(), same(config));
        verify(processedPaymentEventRepository).saveAndFlush(any());
    }
}
