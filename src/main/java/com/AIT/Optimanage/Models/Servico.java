package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FilterDef(name = "ownerFilter", parameters = @ParamDef(name = "userId", type = Integer.class))
@Filter(name = "ownerFilter", condition = "owner_user_id = :userId")
public class Servico extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser != null ? ownerUser.getId() : null;
    }

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
    private String nome;
    private String descricao;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custo;
    private Boolean disponivelVenda;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorVenda;
    @Column(nullable = false)
    private Integer tempoExecucao;
    private Boolean terceirizado;
    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Compatibilidade> compatibilidades;

}