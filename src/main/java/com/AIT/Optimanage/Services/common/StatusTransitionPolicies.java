package com.AIT.Optimanage.Services.common;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;

import java.math.BigDecimal;
import java.util.EnumSet;

import static com.AIT.Optimanage.Services.common.StatusTransitionPolicy.allowFrom;
import static com.AIT.Optimanage.Services.common.StatusTransitionPolicy.forbidFrom;
import static com.AIT.Optimanage.Services.common.StatusTransitionPolicy.impossible;
import static com.AIT.Optimanage.Services.common.StatusTransitionPolicy.requireCondition;

public final class StatusTransitionPolicies {

    private static final StatusTransitionPolicy<StatusCompra, Compra> COMPRA_POLICY =
            StatusTransitionPolicy.<StatusCompra, Compra>builder(StatusCompra.class, "compra")
                    .forTarget(StatusCompra.ORCAMENTO, impossible("Não é possível voltar para o status ORÇAMENTO."))
                    .forTarget(StatusCompra.AGUARDANDO_EXECUCAO,
                            allowFrom(EnumSet.of(StatusCompra.ORCAMENTO),
                                    "Só é possível transformar um orçamento em pedido se estiver no status ORÇAMENTO."))
                    .forTarget(StatusCompra.AGENDADA,
                            forbidFrom(EnumSet.of(StatusCompra.CONCRETIZADO, StatusCompra.CANCELADO),
                                    "Não é possivel agendar uma compra cancelada ou concretizada."))
                    .forTarget(StatusCompra.AGUARDANDO_PAG,
                            allowFrom(EnumSet.of(StatusCompra.AGUARDANDO_EXECUCAO, StatusCompra.ORCAMENTO,
                                            StatusCompra.AGENDADA, StatusCompra.PARCIALMENTE_PAGO, StatusCompra.PAGO),
                                    "A compra só pode aguardar pagamento se o pedido já foi realizado."))
                    .forTarget(StatusCompra.PARCIALMENTE_PAGO,
                            allowFrom(EnumSet.of(StatusCompra.AGUARDANDO_PAG, StatusCompra.AGUARDANDO_EXECUCAO),
                                    "Uma compra só pode ser parcialmente paga se estiver aguardando pagamento ou já parcialmente paga."))
                    .forTarget(StatusCompra.PAGO,
                            allowFrom(EnumSet.of(StatusCompra.AGUARDANDO_PAG, StatusCompra.PARCIALMENTE_PAGO),
                                    "A compra só pode ser paga se estiver aguardando pagamento ou parcialmente paga."))
                    .forTarget(StatusCompra.CONCRETIZADO,
                            allowFrom(EnumSet.of(StatusCompra.PAGO, StatusCompra.AGUARDANDO_EXECUCAO, StatusCompra.AGENDADA),
                                    "A compra só pode ser finalizada se estiver paga, aguardando execução ou agendada."),
                            requireCondition((current, target, compra) ->
                                            compra.getValorPendente().compareTo(BigDecimal.ZERO) <= 0,
                                    "A compra não pode ser finalizada enquanto houver saldo pendente."))
                    .forTarget(StatusCompra.CANCELADO,
                            forbidFrom(EnumSet.of(StatusCompra.CONCRETIZADO),
                                    "Uma compra finalizada não pode ser cancelada."))
                    .build();

    private static final StatusTransitionPolicy<StatusVenda, Venda> VENDA_POLICY =
            StatusTransitionPolicy.<StatusVenda, Venda>builder(StatusVenda.class, "venda")
                    .forTarget(StatusVenda.ORCAMENTO, impossible("Não é possível voltar para o status ORÇAMENTO."))
                    .forTarget(StatusVenda.PENDENTE,
                            allowFrom(EnumSet.of(StatusVenda.ORCAMENTO),
                                    "Só é possível transformar um orçamento em venda a partir do status ORCAMENTO."))
                    .forTarget(StatusVenda.AGENDADA,
                            forbidFrom(EnumSet.of(StatusVenda.CONCRETIZADA, StatusVenda.CANCELADA),
                                    "Não é possivel agendar uma venda cancelada ou concretizada."))
                    .forTarget(StatusVenda.AGUARDANDO_PAG,
                            forbidFrom(EnumSet.of(StatusVenda.CONCRETIZADA, StatusVenda.CANCELADA),
                                    "Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de aguardando pagamento."))
                    .forTarget(StatusVenda.PARCIALMENTE_PAGA,
                            forbidFrom(EnumSet.of(StatusVenda.CONCRETIZADA, StatusVenda.CANCELADA),
                                    "Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de parcialmente paga."),
                            requireCondition((current, target, venda) -> venda.getPagamentos() != null && !venda.getPagamentos().isEmpty(),
                                    "A venda não pode ser parcialmente paga sem um pagamento anterior."),
                            requireCondition((current, target, venda) ->
                                            venda.getValorFinal() != null && venda.getValorPendente() != null &&
                                                    venda.getValorFinal().compareTo(venda.getValorPendente()) != 0,
                                    "A venda não pode ser parcialmente paga sem um pagamento anterior."))
                    .forTarget(StatusVenda.PAGA,
                            forbidFrom(EnumSet.of(StatusVenda.CONCRETIZADA, StatusVenda.CANCELADA),
                                    "Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de paga."),
                            requireCondition((current, target, venda) ->
                                            venda.getValorPendente() != null &&
                                                    venda.getValorPendente().compareTo(BigDecimal.ZERO) <= 0,
                                    "A venda não pode ser paga sem o pagamento completo."))
                    .forTarget(StatusVenda.CONCRETIZADA,
                            forbidFrom(EnumSet.of(StatusVenda.CANCELADA),
                                    "Uma venda cancelada não pode ser concretizada."),
                            forbidFrom(EnumSet.of(StatusVenda.ORCAMENTO),
                                    "Um orçamento não pode ser concretizado."),
                            requireCondition((current, target, venda) ->
                                            venda.getValorPendente() != null &&
                                                    venda.getValorPendente().compareTo(BigDecimal.ZERO) <= 0,
                                    "A venda não pode ser concretizada sem o pagamento completo."))
                    .forTarget(StatusVenda.CANCELADA,
                            forbidFrom(EnumSet.of(StatusVenda.CONCRETIZADA),
                                    "Uma venda concretizada não pode ser cancelada."))
                    .build();

    private StatusTransitionPolicies() {
    }

    public static StatusTransitionPolicy<StatusCompra, Compra> compraPolicy() {
        return COMPRA_POLICY;
    }

    public static StatusTransitionPolicy<StatusVenda, Venda> vendaPolicy() {
        return VENDA_POLICY;
    }
}
