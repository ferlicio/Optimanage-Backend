package com.AIT.Optimanage.Models.Fornecedor;

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
public class FornecedorContato extends BaseEntity {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "id", nullable = false)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor != null ? fornecedor.getId() : null;
    }

    @Column(nullable = false, length = 64)
    private String nome;
    @Column(nullable = false, length = 16)
    private String telefone;
    @Column(length = 64)
    private String email;
    @Column(length = 64)
    private String cargo;
    @Column(length = 128)
    private String observacao;

}
