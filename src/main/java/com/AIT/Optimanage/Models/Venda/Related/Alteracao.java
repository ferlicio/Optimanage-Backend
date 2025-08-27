package com.AIT.Optimanage.Models.Venda.Related;

import com.AIT.Optimanage.Models.Enums.StatusAlteracao;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.AIT.Optimanage.Models.BaseEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Alteracao extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", referencedColumnName = "id", nullable = false)
    private Venda venda;

    @JsonProperty("venda_id")
    public Integer getVendaId() {
        return venda.getId();
    }

    @Column(nullable = false)
    private String descricao;
    @Column(nullable = false)
    private LocalDateTime dataAlteracao;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlteracao status;

}