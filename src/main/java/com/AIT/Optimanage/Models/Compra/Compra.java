package com.AIT.Optimanage.Models.Compra;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.OwnableEntity;
import jakarta.validation.constraints.*;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Compra extends AuditableEntity implements OwnableEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor != null ? fornecedor.getId() : null;
    }


    @Min(1)
    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private LocalDate dataEfetuacao;
    private LocalDate dataAgendada;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;
    private String condicaoPagamento;
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPendente;
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