package com.AIT.Optimanage.Models.Compra.Related;

public enum StatusCompra {
    ORCAMENTO,                // Apenas um orçamento, ainda não é uma compra
    AGUARDANDO_EXECUCAO,      // Pedido realizado, aguardando execução (contém serviços)
    AGUARDANDO_PAG,           // Pedido realizado e efetuado, aguardando pagamento
    PARCIALMENTE_PAGO,        // Pedido realizado, recebido e parcialmente pago
    PAGO,                     // Pedido realizado, recebido e pago, mas não concretizado
    CONCRETIZADO,             // Pedido realizado, recebido e pago
    CANCELADO,                // Compra cancelada ou não foi possível finalizar
}
