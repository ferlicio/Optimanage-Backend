package com.AIT.Optimanage.Controllers.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ImportacaoResultado(int totalRegistros, int sucesso, List<String> erros) {

    public ImportacaoResultado {
        erros = erros == null ? List.of() : List.copyOf(erros);
    }

    public boolean possuiErros() {
        return !erros.isEmpty();
    }

    public ImportacaoResultado adicionarErro(String erro) {
        List<String> atualizados = new ArrayList<>(erros);
        atualizados.add(erro);
        return new ImportacaoResultado(totalRegistros, sucesso, Collections.unmodifiableList(atualizados));
    }
}
