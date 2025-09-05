package com.AIT.Optimanage.Payments;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationDTO {
    @NotBlank
    private String paymentIntentId;
    private PaymentProvider provider;
}
