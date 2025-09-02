package com.AIT.Optimanage.Models.User;

import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
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
@FilterDef(name = "ownerUserFilter", parameters = @ParamDef(name = "ownerUserId", type = Integer.class))
@Filter(name = "ownerUserFilter", condition = "owner_user_id = :ownerUserId")
public class UserInfo extends BaseEntity {
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser != null ? ownerUser.getId() : null;
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

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    public void setPlanoAtivoId(Plano planoAtivoId) {
        this.planoAtivoId = planoAtivoId;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

    public void setInscricaoMunicipal(String inscricaoMunicipal) {
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getPermiteOrcamento() {
        return permiteOrcamento;
    }

    public void setPermiteOrcamento(Boolean permiteOrcamento) {
        this.permiteOrcamento = permiteOrcamento;
    }

    public LocalDate getDataAssinatura() {
        return dataAssinatura;
    }

    public void setDataAssinatura(LocalDate dataAssinatura) {
        this.dataAssinatura = dataAssinatura;
    }

    public Float getMetaMensal() {
        return metaMensal;
    }

    public void setMetaMensal(Float metaMensal) {
        this.metaMensal = metaMensal;
    }

    public Float getMetaAnual() {
        return metaAnual;
    }

    public void setMetaAnual(Float metaAnual) {
        this.metaAnual = metaAnual;
    }
}
