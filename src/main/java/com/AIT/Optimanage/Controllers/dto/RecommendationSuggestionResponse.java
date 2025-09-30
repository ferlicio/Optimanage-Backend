package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationSuggestionResponse {

    private boolean bundle;
    private double score;
    private List<ProdutoResponse> produtos;
    private List<ServicoResponse> servicos;

    public List<ProdutoResponse> getProdutos() {
        return produtos == null ? Collections.emptyList() : produtos;
    }

    public List<ServicoResponse> getServicos() {
        return servicos == null ? Collections.emptyList() : servicos;
    }
}
