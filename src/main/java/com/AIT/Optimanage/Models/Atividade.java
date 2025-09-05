package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Enums.AtividadeAplicavelA;
import com.AIT.Optimanage.Models.Enums.TipoAtividade;
import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Atividade extends BaseEntity implements OwnableEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = true)
    private User ownerUser;

    @JsonProperty("owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUser != null ? ownerUser.getId() : null;
    }

    @Column(nullable = false)
    private String nomeAtividade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AtividadeAplicavelA aplicavelA;

}
