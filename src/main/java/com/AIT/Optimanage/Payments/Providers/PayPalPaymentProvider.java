package com.AIT.Optimanage.Payments.Providers;

import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PayPalPaymentProvider implements PaymentProviderStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.PAYPAL;
    }

    private PayPalHttpClient client(PaymentConfig config) {
        PayPalEnvironment environment = "sandbox".equalsIgnoreCase(config.getEnvironment())
                ? new PayPalEnvironment.Sandbox(config.getClientId(), config.getClientSecret())
                : new PayPalEnvironment.Live(config.getClientId(), config.getClientSecret());
        return new PayPalHttpClient(environment);
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request, PaymentConfig config) {
        OrdersCreateRequest createRequest = new OrdersCreateRequest();
        createRequest.prefer("return=representation");
        OrderRequest order = new OrderRequest();
        order.checkoutPaymentIntent("CAPTURE");
        order.purchaseUnits(List.of(new PurchaseUnitRequest()
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(request.getCurrency() == null ? "BRL" : request.getCurrency().toUpperCase())
                        .value(request.getAmount().setScale(2, RoundingMode.HALF_UP).toString()))));
        createRequest.requestBody(order);
        try {
            HttpResponse<Order> response = client(config).execute(createRequest);
            Order result = response.result();
            String approvalUrl = result.links().stream()
                    .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                    .map(LinkDescription::href)
                    .findFirst()
                    .orElse(null);
            return new PaymentResponseDTO(result.id(), approvalUrl, PaymentProvider.PAYPAL);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar pagamento", e);
        }
    }

    @Override
    public PagamentoDTO confirmPayment(String paymentIntentId, PaymentConfig config) {
        OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(paymentIntentId);
        captureRequest.requestBody(new OrderRequest());
        try {
            HttpResponse<Order> response = client(config).execute(captureRequest);
            Order order = response.result();
            boolean completed = "COMPLETED".equalsIgnoreCase(order.status());
            BigDecimal amount = BigDecimal.ZERO;
            if (order.purchaseUnits() != null && !order.purchaseUnits().isEmpty()) {
                amount = new BigDecimal(order.purchaseUnits().get(0).payments().captures().get(0).amount().value());
            }
            return PagamentoDTO.builder()
                    .valorPago(amount)
                    .dataPagamento(LocalDate.now())
                    .formaPagamento(FormaPagamento.CARTAO_CREDITO)
                    .statusPagamento(completed ? StatusPagamento.PAGO : StatusPagamento.PENDENTE)
                    .observacoes("PayPal payment " + order.id())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao confirmar pagamento", e);
        }
    }
}
