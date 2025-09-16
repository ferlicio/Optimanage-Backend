package com.AIT.Optimanage.Models.Venda;

import com.AIT.Optimanage.Models.Produto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class VendaProduto extends AuditableEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", referencedColumnName = "id", nullable = false)
    private Venda venda;

    @JsonProperty("venda_id")
    public Integer getVendaId() {
        return venda != null ? venda.getId() : null;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", referencedColumnName = "id", nullable = false)
    private Produto produto;

    @JsonProperty("produto_id")
    public Integer getProdutoId() {
        return produto != null ? produto.getId() : null;
    }

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 3)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorFinal;

}