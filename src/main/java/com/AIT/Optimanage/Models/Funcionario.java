package com.AIT.Optimanage.Models;

import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Funcionario extends AuditableEntity implements OwnableEntity {
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
