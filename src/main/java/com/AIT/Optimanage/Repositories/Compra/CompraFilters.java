package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Conjunto de filtros reutilizáveis para consultas de {@link Compra}.
 */
public class CompraFilters {

    private CompraFilters() {
        // Classe utilitária
    }

    public static Specification<Compra> hasOrganization(Integer organizationId) {
        return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
    }

    public static Specification<Compra> hasSequencialUsuario(Integer id) {
        return (root, query, cb) -> cb.equal(root.get("sequencialUsuario"), id);
    }

    public static Specification<Compra> hasFornecedor(Integer fornecedorId) {
        return (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), fornecedorId);
    }

    public static Specification<Compra> dataEfetuacaoAfter(LocalDate dataInicial) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEfetuacao"), dataInicial);
    }

    public static Specification<Compra> dataEfetuacaoBefore(LocalDate dataFinal) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEfetuacao"), dataFinal);
    }

    public static Specification<Compra> hasStatus(StatusCompra status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Compra> isPago(Boolean pago) {
        return (root, query, cb) -> pago
                ? cb.lessThanOrEqualTo(root.get("valorPendente"), BigDecimal.ZERO)
                : cb.greaterThan(root.get("valorPendente"), BigDecimal.ZERO);
    }

    public static Specification<Compra> hasFormaPagamento(FormaPagamento formaPagamento) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Compra, CompraPagamento> join = root.join("pagamentos", JoinType.LEFT);
            return cb.equal(join.get("formaPagamento"), formaPagamento);
        };
    }
}

