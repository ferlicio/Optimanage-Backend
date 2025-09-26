package com.AIT.Optimanage.Payments;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import com.AIT.Optimanage.Auth.TokenBlacklistService;
import com.AIT.Optimanage.Config.JwtAuthenticationFilter;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Config.RateLimitingFilter;
import com.AIT.Optimanage.Config.TenantFilter;
import com.AIT.Optimanage.Services.PlanoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

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

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private PlanoService planoService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void stripeWebhookReturnsOk() throws Exception {
        PagamentoDTO dto = PagamentoDTO.builder()
                .valorPago(BigDecimal.TEN)
                .dataPagamento(LocalDate.now())
                .dataVencimento(LocalDate.now())
                .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                .statusPagamento(StatusPagamento.PAGO)
                .build();

        when(paymentConfigService.getConfig(eq(1), eq(PaymentProvider.STRIPE)))
                .thenReturn(PaymentConfig.builder().provider(PaymentProvider.STRIPE).build());
        when(paymentService.handleWebhook(eq(PaymentProvider.STRIPE), anyString(), anyMap(), any(PaymentConfig.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/pagamentos/webhook")
                        .param("provider", "STRIPE")
                        .param("organizationId", "1")
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
                .dataVencimento(LocalDate.now())
                .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(paymentConfigService.getConfig(eq(2), eq(PaymentProvider.PAYPAL)))
                .thenReturn(PaymentConfig.builder().provider(PaymentProvider.PAYPAL).build());
        when(paymentService.handleWebhook(eq(PaymentProvider.PAYPAL), anyString(), anyMap(), any(PaymentConfig.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/pagamentos/webhook")
                        .param("provider", "PAYPAL")
                        .param("organizationId", "2")
                        .header("paypal-transmission-sig", "sig")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"));
    }
}
