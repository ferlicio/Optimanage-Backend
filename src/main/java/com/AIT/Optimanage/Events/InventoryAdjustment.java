package com.AIT.Optimanage.Events;

/**
 * Representa uma movimentação de um produto no estoque.
 */
public record InventoryAdjustment(Integer produtoId, Integer quantidade) {
    public InventoryAdjustment {
        if (produtoId == null) {
            throw new IllegalArgumentException("O identificador do produto é obrigatório.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
