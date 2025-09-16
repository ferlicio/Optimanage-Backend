package com.AIT.Optimanage.Events;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Evento disparado após a criação de uma compra contendo produtos.
 */
public class CompraCriadaEvent {

    private final Integer compraId;
    private final Integer organizationId;
    private final List<InventoryAdjustment> produtos;

    public CompraCriadaEvent(Integer compraId, Integer organizationId, List<InventoryAdjustment> produtos) {
        this.compraId = Objects.requireNonNull(compraId, "O identificador da compra é obrigatório.");
        this.organizationId = organizationId;
        this.produtos = produtos == null ? List.of() : List.copyOf(produtos);
    }

    public Integer getCompraId() {
        return compraId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public List<InventoryAdjustment> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }
}
