package com.AIT.Optimanage.Models.Cliente;

import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FilterDef(name = "ownerFilter", parameters = @ParamDef(name = "userId", type = Integer.class))
@Filter(name = "ownerFilter", condition = "owner_user_id = :userId")
public class Cliente extends BaseEntity {
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
    @JoinColumn(name = "atividade_id", referencedColumnName = "id", nullable = false)
    private Atividade atividade;

    @JsonProperty("atividade_id")
    public Integer getAtividadeId() {
        return atividade != null ? atividade.getId() : null;
    }

    @Column(nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(nullable = false, length = 64)
    private String origem;

    @Column(nullable = false)
    private Boolean ativo = true;
    @Column(length = 64)
    private String nome;
    @Column(length = 55)
    private String nomeFantasia;
    @Column(length = 64)
    private String razaoSocial;
    @Column(length = 14)
    private String cpf;
    @Column(length = 18)
    private String cnpj;
    @Column(length = 18)
    private String inscricaoEstadual;
    @Column(length = 18)
    private String inscricaoMunicipal;
    @Column(length = 64)
    private String site;
    @Column(length = 256)
    private String informacoesAdicionais;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClienteEndereco> enderecos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClienteContato> contatos;

}