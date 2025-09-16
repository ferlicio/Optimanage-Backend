package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.Enums.AtividadeAplicavelA;
import com.AIT.Optimanage.Models.Enums.TipoAtividade;
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
public class Atividade extends AuditableEntity implements OwnableEntity {
    @Column(nullable = false)
    private String nomeAtividade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AtividadeAplicavelA aplicavelA;

}
