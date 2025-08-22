package com.AIT.Optimanage.Models.Compra;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.User.User;
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
public class Compra {
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
    @ManyToOne(fetch = FetchType.LAZY)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor.getId();
    }


    @Min(1)
    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    @Min(0)
    @Column(nullable = false)
    private Double valorFinal;
    private String condicaoPagamento;
    @Min(0)
    @Column(nullable = false)
    private Double valorPendente;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCompra status = StatusCompra.ORCAMENTO;
    private String observacoes;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<CompraProduto> compraProdutos;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<CompraServico> compraServicos;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraPagamento> pagamentos;

}