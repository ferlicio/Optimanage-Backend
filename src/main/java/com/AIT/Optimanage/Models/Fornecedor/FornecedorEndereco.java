package com.AIT.Optimanage.Models.Fornecedor;

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
public class FornecedorEndereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "id", nullable = false)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor.getId();
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