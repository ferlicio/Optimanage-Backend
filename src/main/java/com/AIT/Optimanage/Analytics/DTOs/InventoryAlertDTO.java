package com.AIT.Optimanage.Analytics.DTOs;

import com.AIT.Optimanage.Models.Inventory.InventoryAlertSeverity;
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
public class InventoryAlertDTO {

    private Integer produtoId;
    private String nomeProduto;
    private InventoryAlertSeverity severity;
    private Integer estoqueAtual;
    private Integer estoqueMinimo;
    private Integer prazoReposicaoDias;
    private BigDecimal consumoMedioDiario;
    private Integer diasRestantes;
    private LocalDate dataEstimadaRuptura;
    private Integer quantidadeSugerida;
    private String mensagem;
}
