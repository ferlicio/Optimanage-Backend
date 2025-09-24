package com.AIT.Optimanage.Models.Inventory;

import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.AuditableEntity;
import com.AIT.Optimanage.Models.OwnableEntity;
import com.AIT.Optimanage.Models.Produto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class InventoryAlert extends AuditableEntity implements OwnableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InventoryAlertSeverity severity;

    @Column(name = "dias_restantes")
    private Integer diasRestantes;

    @Column(name = "consumo_medio_diario", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoMedioDiario;

    @Column(name = "estoque_atual", nullable = false)
    private Integer estoqueAtual;

    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo;

    @Column(name = "prazo_reposicao_dias", nullable = false)
    private Integer prazoReposicaoDias;

    @Column(name = "quantidade_sugerida", nullable = false)
    private Integer quantidadeSugerida;

    @Column(name = "data_estimada_ruptura")
    private LocalDate dataEstimadaRuptura;

    @Column(length = 255)
    private String mensagem;
}
