package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProdutoRepositoryConcurrencyTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private User owner;

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(1);
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        owner = User.builder()
                .nome("John")
                .sobrenome("Doe")
                .email("john@doe.com")
                .senha("pwd")
                .role(Role.OPERADOR)
                .build();
        userRepository.save(owner);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Produto novoProduto(int estoqueInicial) {
        Produto produto = Produto.builder()
                .ownerUser(owner)
                .sequencialUsuario(1)
                .codigoReferencia("SKU")
                .nome("Produto")
                .custo(BigDecimal.ONE)
                .valorVenda(BigDecimal.TEN)
                .qtdEstoque(estoqueInicial)
                .ativo(true)
                .build();
        return produtoRepository.save(produto);
    }

    @Test
    void concurrentIncrementDoesNotLoseUpdates() throws InterruptedException {
        Produto produto = novoProduto(0);
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                TenantContext.setTenantId(1);
                try {
                    start.await();
                    transactionTemplate.execute(status -> {
                        produtoRepository.incrementarEstoque(produto.getId(), 1);
                        return null;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    TenantContext.clear();
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertEquals(threads, atualizado.getQtdEstoque());
    }

    @Test
    void concurrentReducePreventsNegativeStock() throws InterruptedException {
        Produto produto = novoProduto(10);
        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                TenantContext.setTenantId(1);
                try {
                    start.await();
                    Integer updated = transactionTemplate.execute(status ->
                            produtoRepository.reduzirEstoque(produto.getId(), 1));
                    if (updated == 1) {
                        success.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    TenantContext.clear();
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertEquals(10, success.get());
        assertEquals(0, atualizado.getQtdEstoque());
    }
}

