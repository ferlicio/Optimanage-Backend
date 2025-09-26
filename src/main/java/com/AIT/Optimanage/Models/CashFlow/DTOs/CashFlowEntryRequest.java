package com.AIT.Optimanage.Models.CashFlow.DTOs;

import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowEntryRequest {

    @NotBlank
    @Size(max = 128)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;

    @NotNull
    private CashFlowType type;

    @NotNull
    private LocalDate movementDate;
}
