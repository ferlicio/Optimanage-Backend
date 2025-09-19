package com.AIT.Optimanage.Models.User;

import jakarta.persistence.*;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.OwnableEntity;
import lombok.*;
import com.AIT.Optimanage.Models.AuditableEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class Contador extends AuditableEntity implements OwnableEntity {
    @Column(nullable = false)
    private Tabela nomeTabela;
    @Column(nullable = false)
    private Integer contagemAtual;

}
