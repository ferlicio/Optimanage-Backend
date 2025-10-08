package com.AIT.Optimanage.Models.Venda;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Venda.Related.Alteracao;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.OwnableEntity;
import jakarta.validation.constraints.*;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Venda extends AuditableEntity implements OwnableEntity {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @JsonProperty("cliente_id")
    public Integer getClienteId() {
        return cliente != null ? cliente.getId() : null;
    }

    @Min(1)
    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    private LocalTime horaAgendada;
    @Column(name = "duracao_estimada")
    private Duration duracaoEstimada;
    @Column(nullable = true)
    private LocalDate dataCobranca;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoGeral;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;
    private String condicaoPagamento;
    @Builder.Default
    @Min(0)
    @Column(nullable = false)
    private Integer alteracoesPermitidas = 0;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPendente;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVenda status = StatusVenda.PENDENTE;
    private String observacoes;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<VendaProduto> vendaProdutos;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<VendaServico> vendaServicos;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Alteracao> alteracoes;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaPagamento> pagamentos;

    public boolean isOrcamento() {
        return this.status.equals(StatusVenda.ORCAMENTO);
    }

    public boolean isPago() {
        return this.valorPendente.compareTo(BigDecimal.ZERO) == 0;
    }

}