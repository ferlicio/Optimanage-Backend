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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
        CurrentUser.clear();
        TenantContext.clear();
        Mockito.reset(servicoRepository, servicoMapper, planoService);
    }

    private void primeCache() {
        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);
        servicoService.listarServicos(alternativeSearch);
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

    @Test
    void cadastrarServicoEvictsAllCachedFilters() {
        primeCache();
        verify(servicoRepository, times(2)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));

        Plano plano = new Plano();
        plano.setMaxServicos(10);
        when(planoService.obterPlanoUsuario(user)).thenReturn(Optional.of(plano));
        when(servicoRepository.countByOrganizationIdAndAtivoTrue(1)).thenReturn(1L);

        servicoService.cadastrarServico(buildRequest());

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        verify(servicoRepository, times(4)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));
    }

    @Test
    void editarServicoEvictsAllCachedFilters() {
        primeCache();
        verify(servicoRepository, times(2)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));

        Servico existente = new Servico();
        existente.setId(10);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(10, 1)).thenReturn(Optional.of(existente));

        servicoService.editarServico(10, buildRequest());

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        verify(servicoRepository, times(4)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));
    }

    @Test
    void excluirServicoEvictsAllCachedFilters() {
        primeCache();
        verify(servicoRepository, times(2)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));

        Servico existente = new Servico();
        existente.setId(20);
        existente.setTenantId(1);
        existente.setAtivo(true);
        when(servicoRepository.findByIdAndOrganizationIdAndAtivoTrue(20, 1)).thenReturn(Optional.of(existente));

        servicoService.excluirServico(20);

        servicoService.listarServicos(defaultSearch);
        servicoService.listarServicos(alternativeSearch);

        verify(servicoRepository, times(4)).findAllByOrganizationIdAndAtivoTrue(eq(1), any(Pageable.class));
    }
}

