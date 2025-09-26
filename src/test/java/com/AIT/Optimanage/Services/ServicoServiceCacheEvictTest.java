package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.CacheConfig;
import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoResponse;
import com.AIT.Optimanage.Mappers.ServicoMapper;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ServicoRepository;
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

@SpringBootTest(classes = {ServicoService.class, CacheConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ServicoServiceCacheEvictTest {

    @Autowired
    private ServicoService servicoService;

    @MockBean
    private ServicoRepository servicoRepository;

    @MockBean
    private ServicoMapper servicoMapper;

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
        when(servicoRepository.findAllByOrganizationIdAndAtivoTrue(anyInt(), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(new Servico())));
        when(servicoMapper.toResponse(any(Servico.class))).thenAnswer(invocation -> new ServicoResponse());
        when(servicoMapper.toEntity(any(ServicoRequest.class))).thenAnswer(invocation -> {
            ServicoRequest request = invocation.getArgument(0);
            Servico servico = new Servico();
            servico.setSequencialUsuario(request.getSequencialUsuario());
            servico.setNome(request.getNome());
            servico.setDescricao(request.getDescricao());
            servico.setCusto(request.getCusto());
            servico.setValorVenda(request.getValorVenda());
            servico.setTempoExecucao(request.getTempoExecucao());
            servico.setAtivo(request.getAtivo() != null ? request.getAtivo() : Boolean.TRUE);
            return servico;
        });
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        Cache cache = tenantCache();
        cache.clear();
        CurrentUser.clear();
        TenantContext.clear();
        Mockito.reset(servicoRepository, servicoMapper, planoService);
    }

    private void primeCache() {
        Cache cache = tenantCache();
        cache.clear();

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        assertThat(cacheEntries()).hasSize(1);
    }

    private ServicoRequest buildRequest() {
        return ServicoRequest.builder()
                .sequencialUsuario(1)
                .nome("Servico")
                .descricao("desc")
                .custo(BigDecimal.ONE)
                .valorVenda(BigDecimal.TEN)
                .tempoExecucao(30)
                .ativo(true)
                .build();
    }

    private Cache tenantCache() {
        Cache cache = cacheManager.getCache("servicos");
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
    void cadastrarServicoEvictsAllCachedFilters() {
        primeCache();

        Plano plano = new Plano();
        plano.setMaxServicos(10);
        when(planoService.obterPlanoUsuario(user)).thenReturn(Optional.of(plano));
        when(servicoRepository.countByOrganizationIdAndAtivoTrue(1)).thenReturn(1L);

        servicoService.cadastrarServico(buildRequest());

        assertCachesEvicted();

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        assertCachesRepopulated();
    }

    @Test
    void editarServicoEvictsAllCachedFilters() {
        primeCache();

        Servico existente = new Servico();
        existente.setId(10);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(10, 1)).thenReturn(Optional.of(existente));

        servicoService.editarServico(10, buildRequest());

        assertCachesEvicted();

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        assertCachesRepopulated();
    }

    @Test
    void excluirServicoEvictsAllCachedFilters() {
        primeCache();

        Servico existente = new Servico();
        existente.setId(20);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(20, 1)).thenReturn(Optional.of(existente));

        servicoService.excluirServico(20);

        assertCachesEvicted();

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        assertCachesRepopulated();
    }
}

