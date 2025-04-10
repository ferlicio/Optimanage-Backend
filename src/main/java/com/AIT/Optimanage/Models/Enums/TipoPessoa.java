package com.AIT.Optimanage.Models.Enums;

public enum TipoPessoa {
    PF("Pessoa Física"),
    PJ("Pessoa Jurídica");

    private final String tipo;

    TipoPessoa(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoCliente(){
        return tipo;
    }
}
