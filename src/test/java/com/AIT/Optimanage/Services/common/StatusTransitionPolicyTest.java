package com.AIT.Optimanage.Services.common;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusTransitionPolicyTest {

    private final StatusTransitionPolicy<StatusCompra, Compra> compraPolicy = StatusTransitionPolicies.compraPolicy();
    private final StatusTransitionPolicy<StatusVenda, Venda> vendaPolicy = StatusTransitionPolicies.vendaPolicy();

    @Test
    void devePermitirTransicaoValidaDeCompra() {
        Compra compra = Compra.builder()
                .status(StatusCompra.ORCAMENTO)
                .valorPendente(BigDecimal.ZERO)
                .build();

        assertDoesNotThrow(() -> compraPolicy.validate(compra.getStatus(), StatusCompra.AGUARDANDO_EXECUCAO, compra));
    }

    @Test
    void deveBloquearMesmaTransicaoDeCompra() {
        Compra compra = Compra.builder()
                .status(StatusCompra.AGUARDANDO_PAG)
                .valorPendente(BigDecimal.TEN)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> compraPolicy.validate(compra.getStatus(), StatusCompra.AGUARDANDO_PAG, compra));
        assertEquals("A compra já está neste status.", exception.getMessage());
    }

    @Test
    void deveImpedirFinalizarCompraComSaldoPendente() {
        Compra compra = Compra.builder()
                .status(StatusCompra.AGUARDANDO_EXECUCAO)
                .valorPendente(BigDecimal.ONE)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> compraPolicy.validate(compra.getStatus(), StatusCompra.CONCRETIZADO, compra));
        assertEquals("A compra não pode ser finalizada enquanto houver saldo pendente.", exception.getMessage());
    }

    @Test
    void devePermitirTransicaoValidaDeVenda() {
        Venda venda = Venda.builder()
                .status(StatusVenda.ORCAMENTO)
                .valorPendente(BigDecimal.ZERO)
                .valorFinal(BigDecimal.ZERO)
                .build();

        assertDoesNotThrow(() -> vendaPolicy.validate(venda.getStatus(), StatusVenda.PENDENTE, venda));
    }

    @Test
    void deveImpedirVendaPagaSemSaldoQuitado() {
        Venda venda = Venda.builder()
                .status(StatusVenda.AGUARDANDO_PAG)
                .valorPendente(BigDecimal.ONE)
                .valorFinal(BigDecimal.TEN)
                .pagamentos(List.of(VendaPagamento.builder().build()))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> vendaPolicy.validate(venda.getStatus(), StatusVenda.PAGA, venda));
        assertEquals("A venda não pode ser paga sem o pagamento completo.", exception.getMessage());
    }

    @Test
    void devePermitirVendaParcialmentePagaComPagamentoAnterior() {
        Venda venda = Venda.builder()
                .status(StatusVenda.AGUARDANDO_PAG)
                .valorPendente(new BigDecimal("50.00"))
                .valorFinal(new BigDecimal("100.00"))
                .pagamentos(List.of(VendaPagamento.builder().build()))
                .build();

        assertDoesNotThrow(() -> vendaPolicy.validate(venda.getStatus(), StatusVenda.PARCIALMENTE_PAGA, venda));
    }

    @Test
    void deveImpedirVendaParcialmentePagaSemPagamentosAnteriores() {
        Venda venda = Venda.builder()
                .status(StatusVenda.AGUARDANDO_PAG)
                .valorPendente(vendaValorTotal())
                .valorFinal(vendaValorTotal())
                .pagamentos(List.of())
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> vendaPolicy.validate(venda.getStatus(), StatusVenda.PARCIALMENTE_PAGA, venda));
        assertEquals("A venda não pode ser parcialmente paga sem um pagamento anterior.", exception.getMessage());
    }

    private BigDecimal vendaValorTotal() {
        return new BigDecimal("100.00");
    }
}
