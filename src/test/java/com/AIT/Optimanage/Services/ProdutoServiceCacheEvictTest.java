package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.CacheConfig;
import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Mappers.ProdutoMapper;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProdutoService.class, CacheConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProdutoServiceCacheEvictTest {

    @Autowired
    private ProdutoService produtoService;

    @MockBean
    private ProdutoRepository produtoRepository;

    @MockBean
    private ProdutoMapper produtoMapper;

    @MockBean
    private PlanoService planoService;

    @Autowired
    private CacheManager cacheManager;

    private User user;
    private Search defaultSearch;
    private Search alternativeSearch;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setTenantId(1);
        CurrentUser.set(user);
        TenantContext.setTenantId(1);

        defaultSearch = Search.builder()
                .page(0)
                .pageSize(10)
                .sort("id")
                .order(Sort.Direction.ASC)
                .build();

        alternativeSearch = defaultSearch.toBuilder()
                .sort("nome")
                .order(Sort.Direction.DESC)
                .build();
        when(produtoRepository.findAllByOrganizationIdAndAtivoTrue(anyInt(), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(new Produto())));
        when(produtoMapper.toResponse(any(Produto.class))).thenAnswer(invocation -> new ProdutoResponse());
        when(produtoMapper.toEntity(any(ProdutoRequest.class))).thenAnswer(invocation -> {
            ProdutoRequest request = invocation.getArgument(0);
            Produto produto = new Produto();
            produto.setSequencialUsuario(request.getSequencialUsuario());
            produto.setCodigoReferencia(request.getCodigoReferencia());
            produto.setNome(request.getNome());
            produto.setCusto(request.getCusto());
            produto.setValorVenda(request.getValorVenda());
            produto.setQtdEstoque(request.getQtdEstoque());
            produto.setAtivo(request.getAtivo() != null ? request.getAtivo() : Boolean.TRUE);
            return produto;
        });
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        Cache cache = tenantCache();
        cache.clear();
        CurrentUser.clear();
        TenantContext.clear();
        Mockito.reset(produtoRepository, produtoMapper, planoService);
    }

    private void primeCache() {
        Cache cache = tenantCache();
        cache.clear();

        produtoService.listarProdutos(defaultSearch);
        produtoService.listarProdutos(alternativeSearch);

        assertThat(cacheEntries()).hasSize(1);
    }

    private ProdutoRequest buildRequest() {
        return ProdutoRequest.builder()
                .sequencialUsuario(1)
                .codigoReferencia("SKU-1")
                .nome("Produto")
                .custo(BigDecimal.ONE)
                .valorVenda(BigDecimal.TEN)
                .qtdEstoque(5)
                .ativo(true)
                .build();
    }

    private Cache tenantCache() {
        Cache cache = cacheManager.getCache("produtos");
        assertThat(cache).as("tenant scoped cache").isNotNull();
        return cache;
    }

    private void assertCachesEvicted() {
        assertThat(cacheEntries()).isEmpty();
    }

    private void assertCachesRepopulated() {
        assertThat(cacheEntries()).hasSize(1);
    }

    private ConcurrentMap<Object, Object> cacheEntries() {
        Cache cache = tenantCache();
        Object nativeCache = cache.getNativeCache();
        assertThat(nativeCache).isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);
        @SuppressWarnings("unchecked")
        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
                (com.github.benmanes.caffeine.cache.Cache<Object, Object>) nativeCache;
        return caffeineCache.asMap();
    }

    @Test
    void cadastrarProdutoEvictsAllCachedFilters() {
        primeCache();

        Plano plano = new Plano();
        plano.setMaxProdutos(10);
        when(planoService.obterPlanoUsuario(user)).thenReturn(Optional.of(plano));
        when(produtoRepository.countByOrganizationIdAndAtivoTrue(1)).thenReturn(1L);

        produtoService.cadastrarProduto(buildRequest());

        assertCachesEvicted();

        produtoService.listarProdutos(defaultSearch);
        produtoService.listarProdutos(alternativeSearch);

        assertCachesRepopulated();
    }

    @Test
    void editarProdutoEvictsAllCachedFilters() {
        primeCache();

        Produto existente = new Produto();
        existente.setId(10);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(produtoRepository.findByIdAndOrganizationIdAndAtivoTrue(10, 1)).thenReturn(Optional.of(existente));

        produtoService.editarProduto(10, buildRequest());

        assertCachesEvicted();

        produtoService.listarProdutos(defaultSearch);
        produtoService.listarProdutos(alternativeSearch);

        assertCachesRepopulated();
    }

    @Test
    void excluirProdutoEvictsAllCachedFilters() {
        primeCache();

        Produto existente = new Produto();
        existente.setId(20);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(produtoRepository.findByIdAndOrganizationIdAndAtivoTrue(20, 1)).thenReturn(Optional.of(existente));

        produtoService.excluirProduto(20);

        assertCachesEvicted();

        produtoService.listarProdutos(defaultSearch);
        produtoService.listarProdutos(alternativeSearch);

        assertCachesRepopulated();
    }
}

