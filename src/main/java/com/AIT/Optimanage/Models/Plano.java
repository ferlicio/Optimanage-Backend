package com.AIT.Optimanage.Models;

import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Plano extends AuditableEntity {
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, precision = 2)
    private Float valor;
    @Column(nullable = false)
    private Integer duracaoDias;
    @Column(nullable = false)
    private Integer qtdAcessos;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxUsuarios = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxProdutos = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxClientes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxFornecedores = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxServicos = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean agendaHabilitada = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean recomendacoesHabilitadas = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pagamentosHabilitados = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean suportePrioritario = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean monitoramentoEstoqueHabilitado = false;

}
