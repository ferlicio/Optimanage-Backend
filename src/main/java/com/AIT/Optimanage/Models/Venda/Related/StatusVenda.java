package com.AIT.Optimanage.Models.Venda.Related;

public enum StatusVenda {
    ORCAMENTO,          // Apenas um orçamento, ainda não é uma venda
    PENDENTE,           // Venda iniciada, mas aguardando confirmação
    AGENDADA,           // Confirmada e agendada para execução
    AGUARDANDO_PAG,     // Efetuada, mas aguardando pagamento
    PARCIALMENTE_PAGA,  // Efetuada, paga parcialmente e realizada
    PAGA,               // Efetuada e paga, mas não realizada
    CONCRETIZADA,       // Efetuada, paga e realizada
    CANCELADA           // Cliente desistiu ou não foi possível finalizar
}
