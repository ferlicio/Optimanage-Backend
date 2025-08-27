package com.AIT.Optimanage.Models.Venda.DTOs;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import jakarta.validation.constraints.*;
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
public class VendaDTO {
    @NotNull
    private Integer clienteId;
    @NotNull
    private LocalDate dataEfetuacao = LocalDate.now();
    private LocalDate dataAgendada = null;
    private LocalDate dataCobranca;
    @DecimalMin(value = "0.0")
    private BigDecimal descontoGeral = BigDecimal.ZERO;
    private String condicaoPagamento;
    @Min(0)
    private Integer alteracoesPermitidas = 0;
    @NotNull
    private StatusVenda status = StatusVenda.PENDENTE;
    private String observacoes;
    private List<VendaProdutoDTO> produtos;
    private List<VendaServicoDTO> servicos;

    public boolean hasNoItems() {
        return produtos.isEmpty() && servicos.isEmpty();
    }
}
