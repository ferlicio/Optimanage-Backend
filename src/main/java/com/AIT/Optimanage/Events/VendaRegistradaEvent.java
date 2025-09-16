package com.AIT.Optimanage.Events;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Evento disparado após o registro de uma venda contendo produtos.
 */
public class VendaRegistradaEvent {

    private final Integer vendaId;
    private final Integer organizationId;
    private final List<InventoryAdjustment> produtos;

    public VendaRegistradaEvent(Integer vendaId, Integer organizationId, List<InventoryAdjustment> produtos) {
        this.vendaId = Objects.requireNonNull(vendaId, "O identificador da venda é obrigatório.");
        this.organizationId = organizationId;
        this.produtos = produtos == null ? List.of() : List.copyOf(produtos);
    }

    public Integer getVendaId() {
        return vendaId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public List<InventoryAdjustment> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }
}
