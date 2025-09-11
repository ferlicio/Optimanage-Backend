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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PixPaymentProvider.class)
@TestPropertySource(properties = {
        "pix.api.url=https://pix.test",
        "pix.api.key=test"
})
class PixPaymentProviderTest {

    @Autowired
    private PixPaymentProvider provider;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void createPaymentCallsApi() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .amount(BigDecimal.ONE)
                .currency("brl")
                .description("Venda 1")
                .provider(PaymentProvider.PIX)
                .build();

        server.expect(requestTo("https://pix.test/payments"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"paymentIntentId\":\"1\",\"clientSecret\":\"sec\",\"provider\":\"PIX\"}", MediaType.APPLICATION_JSON));

        PaymentResponseDTO response = provider.createPayment(request, PaymentConfig.builder().build());
        assertThat(response.getPaymentIntentId()).isEqualTo("1");
    }

    @Test
    void confirmPaymentParsesStatus() {
        server.expect(requestTo("https://pix.test/payments/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"PAID\",\"amount\":1}", MediaType.APPLICATION_JSON));

        PagamentoDTO dto = provider.confirmPayment("1", PaymentConfig.builder().build());
        assertThat(dto.getStatusPagamento()).isEqualTo(StatusPagamento.PAGO);
        assertThat(dto.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
        assertThat(dto.getValorPago()).isEqualTo(BigDecimal.ONE);
    }
}
