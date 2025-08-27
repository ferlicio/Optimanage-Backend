package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Servico {
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
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "id", nullable = true)
    private Fornecedor fornecedor;

    @JsonProperty("fornecedor_id")
    public Integer getFornecedorId() {
        return fornecedor.getId();
    }

    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false)
    private String nome;
    private String descricao;
    @Column(nullable = false, length = 10, precision = 2)
    private Double custo;
    private Boolean disponivelVenda;
    @Column(nullable = false, length = 10, precision = 2)
    private Double valorVenda;
    @Column(nullable = false)
    private Integer tempoExecucao;
    private Boolean terceirizado;
    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Compatibilidade> compatibilidades;

}