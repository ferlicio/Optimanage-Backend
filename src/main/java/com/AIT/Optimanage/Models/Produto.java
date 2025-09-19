package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Produto extends AuditableEntity implements OwnableEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "id", nullable = true)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor != null ? fornecedor.getId() : null;
    }

    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private String codigoReferencia; // Código do fabricante ou SKU
    @Column(nullable = false)
    private String nome;
    private String descricao;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custo;
    private Boolean disponivelVenda;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorVenda;
    @Column(nullable = false)
    private Integer qtdEstoque;
    private Boolean terceirizado;
    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Compatibilidade> compatibilidades;

    @Version
    private Long version;

}
