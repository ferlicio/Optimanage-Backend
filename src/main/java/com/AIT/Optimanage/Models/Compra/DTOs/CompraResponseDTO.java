package com.AIT.Optimanage.Models.Compra.DTOs;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {
    private Integer id;
    @JsonProperty("fornecedor_id")
    private Integer fornecedorId;
    private Integer sequencialUsuario;
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    private BigDecimal valorFinal;
    private String condicaoPagamento;
    private BigDecimal valorPendente;
    private StatusCompra status;
    private String observacoes;
    private List<CompraProdutoResponseDTO> produtos;
    private List<CompraServicoResponseDTO> servicos;
    private List<CompraPagamentoResponseDTO> pagamentos;
}
