package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Events.CompraCriadaEvent;
import com.AIT.Optimanage.Events.VendaRegistradaEvent;
import com.AIT.Optimanage.Models.Inventory.InventoryAction;
import com.AIT.Optimanage.Models.Inventory.InventoryHistory;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Repositories.InventoryHistoryRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final String MANUAL_DESCRIPTION = "Ajuste manual de estoque";

    private final ProdutoRepository produtoRepository;
    private final InventoryHistoryRepository historyRepository;
    private final PlanoAccessGuard planoAccessGuard;

    @Transactional
    public void incrementar(Integer produtoId, Integer quantidade) {
        incrementar(produtoId, quantidade, InventorySource.AJUSTE_MANUAL, null, MANUAL_DESCRIPTION);
    }

    @Transactional
    public void reduzir(Integer produtoId, Integer quantidade) {
        reduzir(produtoId, quantidade, InventorySource.AJUSTE_MANUAL, null, MANUAL_DESCRIPTION);
    }

    @Transactional
    public void incrementar(Integer produtoId, Integer quantidade, InventorySource source, Integer referenciaId, String descricao) {
        ajustarEstoque(produtoId, quantidade, InventoryAction.INCREMENT, source, referenciaId, descricao);
    }

    @Transactional
    public void reduzir(Integer produtoId, Integer quantidade, InventorySource source, Integer referenciaId, String descricao) {
        ajustarEstoque(produtoId, quantidade, InventoryAction.DECREMENT, source, referenciaId, descricao);
    }

    private void ajustarEstoque(Integer produtoId, Integer quantidade, InventoryAction action, InventorySource source,
                                 Integer referenciaId, String descricao) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        Integer organizationId = Optional.ofNullable(TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Organização não definida no contexto."));

        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);

        Produto produto = produtoRepository.findByIdAndOrganizationId(produtoId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));

        int updatedRows = action == InventoryAction.INCREMENT
                ? produtoRepository.incrementarEstoque(produtoId, quantidade, organizationId)
                : produtoRepository.reduzirEstoque(produtoId, quantidade, organizationId);

        if (updatedRows == 0) {
            String mensagem = action == InventoryAction.INCREMENT
                    ? "Falha ao incrementar estoque do produto " + produto.getNome()
                    : "Estoque insuficiente para o produto " + produto.getNome();
            log.warn(mensagem + " (id: {})", produtoId);
            throw new IllegalArgumentException(mensagem);
        }

        registrarHistorico(produto, quantidade, action, source, referenciaId, descricao);
        log.info("Estoque do produto {} {} em {} unidades (origem: {}, referência: {})",
                produtoId,
                action == InventoryAction.INCREMENT ? "incrementado" : "reduzido",
                quantidade,
                source,
                referenciaId);
    }

    private void registrarHistorico(Produto produto, Integer quantidade, InventoryAction action, InventorySource source,
                                    Integer referenciaId, String descricao) {
        InventoryHistory historico = InventoryHistory.builder()
                .produto(produto)
                .action(action)
                .source(source)
                .referenceId(referenciaId)
                .quantidade(quantidade)
                .descricao(Optional.ofNullable(descricao).orElse(source.name()))
                .build();
        historico.setTenantId(produto.getOrganizationId());
        historyRepository.save(historico);
    }

    @Transactional
    @EventListener
    public void aoCriarCompra(CompraCriadaEvent event) {
        if (event.getProdutos().isEmpty()) {
            return;
        }
        executarNoTenant(event.getOrganizationId(), () -> event.getProdutos().forEach(produto ->
                incrementar(produto.produtoId(), produto.quantidade(), InventorySource.COMPRA,
                        event.getCompraId(), "Compra #" + event.getCompraId())));
    }

    @Transactional
    @EventListener
    public void aoRegistrarVenda(VendaRegistradaEvent event) {
        if (event.getProdutos().isEmpty()) {
            return;
        }
        executarNoTenant(event.getOrganizationId(), () -> event.getProdutos().forEach(produto ->
                reduzir(produto.produtoId(), produto.quantidade(), InventorySource.VENDA,
                        event.getVendaId(), "Venda #" + event.getVendaId())));
    }

    private void executarNoTenant(Integer organizationId, Runnable runnable) {
        Integer anterior = TenantContext.getTenantId();
        try {
            if (organizationId != null) {
                TenantContext.setTenantId(organizationId);
            }
            runnable.run();
        } finally {
            if (anterior != null) {
                TenantContext.setTenantId(anterior);
            } else {
                TenantContext.clear();
            }
        }
    }
}
