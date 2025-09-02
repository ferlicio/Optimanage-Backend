package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FilterDef(name = "ownerUserFilter", parameters = @ParamDef(name = "ownerUserId", type = Integer.class))
@Filter(name = "ownerUserFilter", condition = "owner_user_id = :ownerUserId")
public class Funcionario extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser != null ? ownerUser.getId() : null;
    }

    @Column(nullable = false)
    private Integer sequencialUsuario;
    @Column(nullable = false, length = 64)
    private String nome;
    @Column(nullable = false, length = 64)
    private String email;
    @Column(nullable = false)
    private String senha;
    @Column(nullable = false)
    private Boolean ativo;

}
