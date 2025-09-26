package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BoletoPaymentProvider.class)
@TestPropertySource(properties = {
        "boleto.api.url=https://boleto.test",
        "boleto.api.key=test"
})
class BoletoPaymentProviderTest {

    @TestConfiguration
    static class RestTemplateConfig {
        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }

    @Autowired
    private BoletoPaymentProvider provider;

    @Autowired
    private MockRestServiceServer server;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createPaymentCallsApi() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .amount(BigDecimal.TEN)
                .currency("brl")
                .description("Venda 1")
                .provider(PaymentProvider.BOLETO)
                .build();

        server.expect(requestTo("https://boleto.test/payments"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"paymentIntentId\":\"1\",\"clientSecret\":\"sec\",\"provider\":\"BOLETO\"}", MediaType.APPLICATION_JSON));

        PaymentResponseDTO response = provider.createPayment(request, PaymentConfig.builder().build());
        assertThat(response.getPaymentIntentId()).isEqualTo("1");
    }

    @Test
    void confirmPaymentParsesStatus() {
        server.expect(requestTo("https://boleto.test/payments/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"PAID\",\"amount\":10}", MediaType.APPLICATION_JSON));

        PagamentoDTO dto = provider.confirmPayment("1", PaymentConfig.builder().build());
        assertThat(dto.getStatusPagamento()).isEqualTo(StatusPagamento.PAGO);
        assertThat(dto.getFormaPagamento()).isEqualTo(FormaPagamento.BOLETO);
        assertThat(dto.getValorPago()).isEqualTo(BigDecimal.TEN);
    }
}
