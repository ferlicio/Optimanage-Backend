package com.AIT.Optimanage.Models.Cliente;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ClienteEndereco extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @JsonProperty("cliente_id")
    public Integer getClienteId() {
        return cliente != null ? cliente.getId() : null;
    }

    @Column(nullable = false, length = 64)
    private String nomeUnidade;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 64)
    private String cidade;

    @Column(nullable = false, length = 64)
    private String bairro;

    @Column(nullable = false, length = 64)
    private String logradouro;

    @Column(nullable = false)
    private Integer numero;

    @Column(length = 64)
    private String complemento;

}