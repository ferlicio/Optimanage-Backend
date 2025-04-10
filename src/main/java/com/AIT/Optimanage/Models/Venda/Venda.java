package com.AIT.Optimanage.Models.Venda;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Related.Alteracao;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser.getId();
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @JsonProperty("cliente_id")
    public Integer getClienteId() {
        return cliente.getId();
    }

    @Min(1)
    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    @Column(nullable = false)
    private LocalDate dataCobranca;
    @Min(0)
    @Column(nullable = false)
    private Double valorTotal;
    @Min(0)
    @Column(nullable = false)
    private Double descontoGeral;
    @Min(0)
    @Column(nullable = false)
    private Double valorFinal;
    private String condicaoPagamento;
    @Min(0)
    @Column(nullable = false)
    private Integer alteracoesPermitidas = 0;
    @Min(0)
    @Column(nullable = false)
    private Double valorPendente;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVenda status = StatusVenda.PENDENTE;
    private String observacoes;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaProduto> vendaProdutos;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaServico> vendaServicos;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Alteracao> alteracoes;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaPagamento> pagamentos;

    public boolean isOrcamento() {
        return this.status.equals(StatusVenda.PENDENTE);
    }

    public boolean isPago() {
        return this.valorPendente == 0.0;
    }

}