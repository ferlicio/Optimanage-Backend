package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Funcionario {
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
