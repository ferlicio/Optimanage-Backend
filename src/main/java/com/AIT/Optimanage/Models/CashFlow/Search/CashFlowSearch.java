package com.AIT.Optimanage.Models.CashFlow.Search;

import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import com.AIT.Optimanage.Models.Search;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowSearch extends Search {
    private CashFlowType type;
    private CashFlowStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}
