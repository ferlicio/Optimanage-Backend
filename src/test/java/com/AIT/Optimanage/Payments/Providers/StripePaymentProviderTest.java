package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripePaymentProviderTest {

    private final StripePaymentProvider provider = new StripePaymentProvider();

    @Test
    void handleWebhookUsesConfiguredWebhookSecret() throws Exception {
        PaymentConfig config = PaymentConfig.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("sk_test_dummy")
                .clientSecret("whsec_test_secret")
                .build();

        String payload = "{\"id\":\"evt_test\",\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test\",\"object\":\"payment_intent\",\"amount_received\":1500}}}";
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        String signature = signPayload(signedPayload, config.getClientSecret());
        Map<String, String> headers = Map.of("Stripe-Signature", "t=" + timestamp + ",v1=" + signature);

        PagamentoDTO resultado = provider.handleWebhook(payload, headers, config);

        assertThat(resultado.getStatusPagamento()).isEqualTo(StatusPagamento.PAGO);
        assertThat(resultado.getValorPago()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    void handleWebhookWithoutSecretThrowsHelpfulError() {
        PaymentConfig config = PaymentConfig.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("sk_test_dummy")
                .build();

        Map<String, String> headers = Map.of("Stripe-Signature", "t=0,v1=invalid");

        assertThatThrownBy(() -> provider.handleWebhook("{}", headers, config))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("webhook secret");
    }

    private String signPayload(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(signature);
    }
}
