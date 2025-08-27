package com.AIT.Optimanage.Analytics.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumoDTO {
    private BigDecimal totalVendas;
    private BigDecimal totalCompras;
    private BigDecimal lucro;
}

