package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Enums.AtividadeAplicavelA;
import com.AIT.Optimanage.Models.Enums.TipoAtividade;
import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Atividade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
