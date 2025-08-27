package com.AIT.Optimanage.Models.Compra;

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
public class CompraProduto extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", referencedColumnName = "id", nullable = false)
    private Compra compra;

    @JsonProperty("compra_id")
    public Integer getCompraId() {
        return compra.getId();
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

    @Column(nullable = false)
    private Double valorTotal;

}
