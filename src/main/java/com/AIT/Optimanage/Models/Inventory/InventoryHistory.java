package com.AIT.Optimanage.Models.Inventory;

import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.AuditableEntity;
import com.AIT.Optimanage.Models.OwnableEntity;
import com.AIT.Optimanage.Models.Produto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Histórico de movimentações de estoque.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class InventoryHistory extends AuditableEntity implements OwnableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", referencedColumnName = "id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventorySource source;

    @Column(name = "source_reference_id")
    private Integer referenceId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 255)
    private String descricao;
}
