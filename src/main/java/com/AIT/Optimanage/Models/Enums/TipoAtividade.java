package com.AIT.Optimanage.Models.Enums;

public enum TipoAtividade {
    PADRAO("Padrão"),
    PERSONALIZADO("Personalizado");

    private final String tipo;

    TipoAtividade(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoAtividade(){
        return tipo;
    }
}
