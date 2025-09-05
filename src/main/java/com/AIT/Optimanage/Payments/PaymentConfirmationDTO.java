package com.AIT.Optimanage.Payments;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationDTO {
    @NotBlank
    private String paymentIntentId;
}
