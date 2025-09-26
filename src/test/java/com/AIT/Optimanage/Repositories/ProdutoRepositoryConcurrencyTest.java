package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@ActiveProfiles("test")
class ProdutoRepositoryConcurrencyTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Produto novoProduto(int estoqueInicial) {
        Produto produto = Produto.builder()
                .sequencialUsuario(1)
                .codigoReferencia("SKU")
                .nome("Produto")
                .custo(BigDecimal.ONE)
                .valorVenda(BigDecimal.TEN)
                .qtdEstoque(estoqueInicial)
                .ativo(true)
                .build();
        produto.setTenantId(1);
        return produtoRepository.save(produto);
    }

    @Test
    void incrementUpdatesStockCount() {
        Produto produto = novoProduto(0);
        int increments = 10;

        for (int i = 0; i < increments; i++) {
            produtoRepository.incrementarEstoque(produto.getId(), 1, 1);
        }

        entityManager.flush();
        entityManager.clear();

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertThat(atualizado.getQtdEstoque()).isEqualTo(increments);
    }

    @Test
    void reduceStockStopsAtZero() {
        Produto produto = novoProduto(10);
        int attempts = 20;
        int successful = 0;

        for (int i = 0; i < attempts; i++) {
            successful += produtoRepository.reduzirEstoque(produto.getId(), 1, 1);
        }

        entityManager.flush();
        entityManager.clear();

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertThat(successful).isEqualTo(10);
        assertThat(atualizado.getQtdEstoque()).isZero();
    }
}

