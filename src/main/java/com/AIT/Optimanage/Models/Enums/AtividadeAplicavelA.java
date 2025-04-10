package com.AIT.Optimanage.Models.Enums;

public enum AtividadeAplicavelA {
    PF("Pessoa Física"),
    PJ("Pessoa Jurídica"),
    AMBOS("Ambos");

    private final String tipo;

    AtividadeAplicavelA(String tipo) {
        this.tipo = tipo;
    }

    public String getAtividadeAplicavelA(){
        return tipo;
    }
}
