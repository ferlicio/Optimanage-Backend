package com.AIT.Optimanage.Models.Venda.DTOs;

import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaResponseDTO {
    private Integer id;
    @JsonProperty("cliente_id")
    private Integer clienteId;
    private Integer sequencialUsuario;
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    private LocalTime horaAgendada;
    private Duration duracaoEstimada;
    private LocalDate dataCobranca;
    private BigDecimal valorTotal;
    private BigDecimal descontoGeral;
    private BigDecimal valorFinal;
    private String condicaoPagamento;
    private Integer alteracoesPermitidas;
    private BigDecimal valorPendente;
    private StatusVenda status;
    private String observacoes;
    private List<VendaProdutoResponseDTO> produtos;
    private List<VendaServicoResponseDTO> servicos;
    private List<VendaPagamentoResponseDTO> pagamentos;
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
