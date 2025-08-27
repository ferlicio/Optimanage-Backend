package com.AIT.Optimanage.Models.Compra;

import com.AIT.Optimanage.Models.Servico;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CompraServico {
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
    @JoinColumn(name = "servico_id", referencedColumnName = "id", nullable = false)
    private Servico servico;

    @JsonProperty("servico_id")
    public Integer getServicoId() {
        return servico.getId();
    }

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

}