package com.AIT.Optimanage.Models.CashFlow.DTOs;

import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowEntryResponse {
    private Integer id;
    private String description;
    private BigDecimal amount;
    private CashFlowType type;
    private CashFlowStatus status;
    private LocalDate movementDate;
    private LocalDateTime cancelledAt;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
}
