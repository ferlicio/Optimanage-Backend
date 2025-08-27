package com.AIT.Optimanage.Models.Venda;

import com.AIT.Optimanage.Models.Produto;
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
public class VendaProduto extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", referencedColumnName = "id", nullable = false)
    private Venda venda;

    @JsonProperty("venda_id")
    public Integer getVendaId() {
        return venda.getId();
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", referencedColumnName = "id", nullable = false)
    private Produto produto;

    @JsonProperty("produto_id")
    public Integer getProdutoId() {
        return produto.getId();
    }

    @Column(length = 10, precision = 2, nullable = false)
    private Double valorUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 3)
    private Double desconto = 0.0;

    @Column(length = 10,precision = 2)
    private Double valorFinal;

}