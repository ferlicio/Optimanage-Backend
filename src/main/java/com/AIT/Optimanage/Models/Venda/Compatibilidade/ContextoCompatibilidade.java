package com.AIT.Optimanage.Models.Venda.Compatibilidade;

import com.AIT.Optimanage.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.AIT.Optimanage.Models.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FilterDef(name = "ownerUserFilter", parameters = @ParamDef(name = "ownerUserId", type = Integer.class))
@Filter(name = "ownerUserFilter", condition = "owner_user_id = :ownerUserId")
public class ContextoCompatibilidade extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    private User ownerUser;

    @Column(nullable = false)
    private String nome; // Ex: "Ford Fiesta 1.6 2014", "Windows 10", "Idosos acima de 60 anos"
}
