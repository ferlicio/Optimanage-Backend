package com.AIT.Optimanage.Models.Cliente;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ClienteContato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @JsonProperty("cliente_id")
    public Integer getClienteId() {
        return cliente.getId();
    }

    @Column(nullable = false, length = 64)
    private String nome;
    @Column(nullable = false, length = 16)
    private String telefone;
    @Column(length = 64)
    private String email;
    @Column(length = 64)
    private String cargo;
    @Column(length = 10)
    private String aniversario;
    @Column(length = 128)
    private String observacao;

}