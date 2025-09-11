package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PagamentoWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentConfigService paymentConfigService;

    @Test
    void stripeWebhookReturnsOk() throws Exception {
        PagamentoDTO dto = PagamentoDTO.builder()
                .valorPago(BigDecimal.TEN)
                .dataPagamento(LocalDate.now())
                .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                .statusPagamento(StatusPagamento.PAGO)
                .build();

        when(paymentConfigService.getConfig(PaymentProvider.STRIPE)).thenReturn(PaymentConfig.builder().provider(PaymentProvider.STRIPE).build());
        when(paymentService.handleWebhook(eq(PaymentProvider.STRIPE), anyString(), anyMap(), any(PaymentConfig.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/pagamentos/webhook")
                        .param("provider", "STRIPE")
                        .header("Stripe-Signature", "sig")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("PAGO"));
    }

    @Test
    void paypalWebhookReturnsOk() throws Exception {
        PagamentoDTO dto = PagamentoDTO.builder()
                .valorPago(BigDecimal.ONE)
                .dataPagamento(LocalDate.now())
                .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(paymentConfigService.getConfig(PaymentProvider.PAYPAL)).thenReturn(PaymentConfig.builder().provider(PaymentProvider.PAYPAL).build());
        when(paymentService.handleWebhook(eq(PaymentProvider.PAYPAL), anyString(), anyMap(), any(PaymentConfig.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/pagamentos/webhook")
                        .param("provider", "PAYPAL")
                        .header("paypal-transmission-sig", "sig")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"));
    }
}
