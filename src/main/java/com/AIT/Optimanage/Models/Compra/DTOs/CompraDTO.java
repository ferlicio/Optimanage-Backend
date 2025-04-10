package com.AIT.Optimanage.Models.Compra.DTOs;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.PagamentoDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDTO {
    @NotNull
    private Integer fornecedorId;
    @NotNull
    private LocalDate dataEfetuacao = LocalDate.now();
    private LocalDate dataAgendada = null;
    private LocalDate dataCobranca;
    @Min(0)
    @NotNull
    private Double valorFinal;
    private String condicaoPagamento;
    @NotNull
    private StatusCompra status = StatusCompra.ORCAMENTO;
    private String observacoes;
    private List<CompraProdutoDTO> produtos;
    private List<CompraServicoDTO> servicos;

    public boolean hasNoItems() {
        return produtos.isEmpty() && servicos.isEmpty();
    }
}
