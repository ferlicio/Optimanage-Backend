package com.AIT.Optimanage.Models.Organization;

import com.AIT.Optimanage.Models.BaseEntity;
import com.AIT.Optimanage.Models.OwnableEntity;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Organization extends BaseEntity implements OwnableEntity {
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_contact_id", referencedColumnName = "id")
    private User primaryContact;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser != null ? ownerUser.getId() : null;
    }

    @JsonProperty("primary_contact_id")
    public Integer getPrimaryContactId() {
        return primaryContact != null ? primaryContact.getId() : null;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tipo_acesso_id", referencedColumnName = "id", nullable = false)
    private Plano planoAtivoId;

    @JsonProperty("tipo_acesso_id")
    public Integer getPlanoAtivoId() {
        return planoAtivoId != null ? planoAtivoId.getId() : null;
    }

    @Column(nullable = false)
    private String cnpj;
    @Column(nullable = false)
    private String razaoSocial;
    @Column(nullable = false)
    private String nomeFantasia;
    private String inscricaoEstadual;
    private String inscricaoMunicipal;
    private String telefone;
    private String email;
    @Column(nullable = false)
    private Boolean permiteOrcamento;
    @Column(nullable = false)
    private LocalDate dataAssinatura;

    @Column(precision = 2)
    private Float metaMensal;

    @Column(precision = 2)
    private Float metaAnual;
}

