package com.AIT.Optimanage.Payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String paymentIntentId;
    private String clientSecret;
    private PaymentProvider provider;
}
