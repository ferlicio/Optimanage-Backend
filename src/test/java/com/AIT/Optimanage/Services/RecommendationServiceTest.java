package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.RecommendationProperties;
import com.AIT.Optimanage.Controllers.dto.RecommendationSuggestionResponse;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import com.AIT.Optimanage.Models.Inventory.InventoryAlertSeverity;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.Venda.CompatibilidadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private PlanoService planoService;
    @Mock
    private CompatibilidadeService compatibilidadeService;
    @Mock
    private InventoryMonitoringService inventoryMonitoringService;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private RecommendationProperties recommendationProperties;

    @InjectMocks
    private RecommendationService recommendationService;

    private User loggedUser;

    @BeforeEach
    void setUp() {
        loggedUser = new User();
        loggedUser.setTenantId(1);
        CurrentUser.set(loggedUser);
        lenient().when(inventoryMonitoringService.listarAlertasOrganizacao(1)).thenReturn(Collections.emptyList());
        lenient().when(recommendationProperties.getHistoryWindowDays()).thenReturn(365);
        lenient().when(recommendationProperties.getChurnWeight()).thenReturn(0.5);
        lenient().when(recommendationProperties.getRotatividadeWeight()).thenReturn(0.3);
        lenient().when(recommendationProperties.getProdutoMargemWeight()).thenReturn(1.0);
        lenient().when(recommendationProperties.getServicoMargemWeight()).thenReturn(1.0);
        lenient().when(recommendationProperties.getBundleWeight()).thenReturn(1.2);
        lenient().when(servicoRepository.findAllById(anyCollection())).thenReturn(Collections.emptyList());
    }

    private void stubPlano(boolean habilitado) {
        Plano plano = new Plano();
        plano.setRecomendacoesHabilitadas(habilitado);
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.of(plano));
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void recomendarProdutosIgnoraItensSemEstoque() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 5L});
        when(vendaRepository.findTopProdutosByCliente(123, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(500));
        when(clienteRepository.findByIdAndOrganizationId(123, 1)).thenReturn(Optional.of(cliente));

        Venda vendaRecente = criarVenda(LocalDate.of(2024, 1, 10),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 2));
        Venda vendaAntiga = criarVenda(LocalDate.of(2023, 12, 1),
                criarVendaProduto(100, 1),
                criarVendaProduto(300, 1));

        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(vendaRecente, vendaAntiga));

        Produto disponivel = produtoComEstoque(200, 5, BigDecimal.valueOf(15), BigDecimal.valueOf(30));
        Produto semEstoque = produtoComEstoque(300, 0, BigDecimal.valueOf(15), BigDecimal.valueOf(30));

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(disponivel, semEstoque));

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(123);

        assertEquals(1, recomendados.size());
        RecommendationSuggestionResponse sugestao = recomendados.get(0);
        assertFalse(sugestao.isBundle());
        assertEquals(200, sugestao.getProdutos().get(0).getId());

        ArgumentCaptor<Collection<Integer>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(produtoRepository).findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(captor.capture(), eq(1));
        assertTrue(captor.getValue().contains(200));
        verify(compatibilidadeService, never()).buscarCompatibilidades(any(User.class), anyString());
    }

    @Test
    void recomendarProdutosPriorizaItensCompativeis() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 3L});
        when(vendaRepository.findTopProdutosByCliente(321, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(500));
        when(clienteRepository.findByIdAndOrganizationId(321, 1)).thenReturn(Optional.of(cliente));

        Venda venda = criarVenda(LocalDate.of(2024, 1, 10),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 1),
                criarVendaProduto(300, 1));

        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(venda));

        Produto produtoA = produtoComEstoque(200, 10, BigDecimal.valueOf(10), BigDecimal.valueOf(20));
        Produto produtoB = produtoComEstoque(300, 10, BigDecimal.valueOf(5), BigDecimal.valueOf(25));

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(produtoA, produtoB));

        Compatibilidade compatibilidade = Compatibilidade.builder()
                .produto(produtoB)
                .compativel(true)
                .build();
        when(compatibilidadeService.buscarCompatibilidades(loggedUser, "Contexto A"))
                .thenReturn(List.of(compatibilidade));

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(321, "Contexto A");

        assertEquals(2, recomendados.size());
        assertEquals(300, recomendados.get(0).getProdutos().get(0).getId());
    }

    @Test
    void recomendarProdutosLimitaQuantidadeDeSugestoes() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 1L});
        when(vendaRepository.findTopProdutosByCliente(999, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(500));
        when(clienteRepository.findByIdAndOrganizationId(999, 1)).thenReturn(Optional.of(cliente));

        List<VendaProduto> itens = new ArrayList<>();
        itens.add(criarVendaProduto(100, 1));
        for (int i = 0; i < 12; i++) {
            itens.add(criarVendaProduto(200 + i, 1));
        }
        Venda venda = criarVenda(LocalDate.of(2024, 1, 1), itens.toArray(new VendaProduto[0]));

        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(venda));

        List<Produto> produtos = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            produtos.add(produtoComEstoque(200 + i, 2, BigDecimal.valueOf(5), BigDecimal.valueOf(12)));
        }

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(produtos);

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(999);

        assertEquals(10, recomendados.size());
        assertTrue(recomendados.stream().allMatch(sugestao -> !sugestao.isBundle()));
        assertTrue(recomendados.stream()
                .flatMap(sugestao -> sugestao.getProdutos().stream())
                .allMatch(produto -> produto.getQtdEstoque() != null && produto.getQtdEstoque() > 0));
    }

    @Test
    void recomendarProdutosIncluiBundlesDisponiveis() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 2L});
        when(vendaRepository.findTopProdutosByCliente(10, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(300));
        when(clienteRepository.findByIdAndOrganizationId(10, 1)).thenReturn(Optional.of(cliente));

        Venda venda = criarVenda(LocalDate.of(2024, 2, 15),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 1));
        VendaServico vendaServico = criarVendaServico(900, 1);
        vendaServico.setVenda(venda);
        venda.setVendaServicos(List.of(vendaServico));

        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(venda));

        Produto produto = produtoComEstoque(200, 4, BigDecimal.valueOf(20), BigDecimal.valueOf(60));
        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(produto));

        Servico servico = servicoDisponivel(900, true);
        when(servicoRepository.findAllById(anyCollection())).thenReturn(List.of(servico));

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(10, null, true, true);

        assertEquals(1, recomendados.size());
        RecommendationSuggestionResponse bundle = recomendados.get(0);
        assertTrue(bundle.isBundle());
        assertEquals(200, bundle.getProdutos().get(0).getId());
        assertEquals(900, bundle.getServicos().get(0).getId());
    }

    @Test
    void recomendarProdutosFiltraBundlesIndisponiveis() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 1L});
        when(vendaRepository.findTopProdutosByCliente(11, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(150));
        when(clienteRepository.findByIdAndOrganizationId(11, 1)).thenReturn(Optional.of(cliente));

        Venda venda = criarVenda(LocalDate.of(2024, 3, 1),
                criarVendaProduto(100, 1),
                criarVendaProduto(210, 1));
        VendaServico vendaServico = criarVendaServico(901, 1);
        vendaServico.getServico().setDisponivelVenda(false);
        vendaServico.getServico().setAtivo(true);
        vendaServico.setVenda(venda);
        venda.setVendaServicos(List.of(vendaServico));

        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(venda));

        Produto produto = produtoComEstoque(210, 3, BigDecimal.valueOf(12), BigDecimal.valueOf(40));
        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(produto));

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(11, null, true, true);

        assertTrue(recomendados.isEmpty());
    }

    @Test
    void recomendarProdutosEvitaItensComRupturaPrevista() {
        stubPlano(true);
        List<Object[]> historicoCliente = Collections.singletonList(new Object[]{100, 2L});
        when(vendaRepository.findTopProdutosByCliente(42, 1)).thenReturn(historicoCliente);

        Cliente cliente = clienteAtivo(BigDecimal.ZERO, BigDecimal.valueOf(500));
        when(clienteRepository.findByIdAndOrganizationId(42, 1)).thenReturn(Optional.of(cliente));

        Venda venda = criarVenda(LocalDate.of(2024, 4, 1),
                criarVendaProduto(100, 1),
                criarVendaProduto(600, 1),
                criarVendaProduto(700, 1));
        when(vendaRepository.findRecentWithItensByOrganization(eq(1), any(LocalDate.class)))
                .thenReturn(List.of(venda));

        Produto saudavel = produtoComEstoque(600, 10, BigDecimal.valueOf(8), BigDecimal.valueOf(18));
        Produto emRisco = produtoComEstoque(700, 10, BigDecimal.valueOf(7), BigDecimal.valueOf(17));
        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(saudavel, emRisco));

        InventoryAlert alerta = InventoryAlert.builder()
                .produto(emRisco)
                .severity(InventoryAlertSeverity.CRITICAL)
                .build();
        when(inventoryMonitoringService.listarAlertasOrganizacao(1)).thenReturn(List.of(alerta));

        List<RecommendationSuggestionResponse> recomendados = recommendationService.recomendarProdutos(42);

        assertEquals(1, recomendados.size());
        assertEquals(600, recomendados.get(0).getProdutos().get(0).getId());
    }

    @Test
    void recomendarProdutosRespeitaPlano() {
        stubPlano(false);

        assertThrows(AccessDeniedException.class, () -> recommendationService.recomendarProdutos(1));
    }

    private Venda criarVenda(LocalDate dataEfetuacao, VendaProduto... itens) {
        Venda venda = new Venda();
        venda.setDataEfetuacao(dataEfetuacao);
        List<VendaProduto> vendaProdutos = new ArrayList<>();
        for (VendaProduto item : itens) {
            item.setVenda(venda);
            vendaProdutos.add(item);
        }
        venda.setVendaProdutos(vendaProdutos);
        return venda;
    }

    private VendaProduto criarVendaProduto(int produtoId, int quantidade) {
        Produto produto = Produto.builder()
                .custo(BigDecimal.TEN)
                .valorVenda(BigDecimal.valueOf(20))
                .build();
        produto.setId(produtoId);
        produto.setOrganizationId(1);
        VendaProduto vendaProduto = new VendaProduto();
        vendaProduto.setProduto(produto);
        vendaProduto.setQuantidade(quantidade);
        return vendaProduto;
    }

    private VendaServico criarVendaServico(int servicoId, int quantidade) {
        Servico servico = servicoDisponivel(servicoId, true);
        VendaServico vendaServico = new VendaServico();
        vendaServico.setServico(servico);
        vendaServico.setQuantidade(quantidade);
        return vendaServico;
    }

    private Produto produtoComEstoque(int id, int estoque, BigDecimal custo, BigDecimal valorVenda) {
        Produto produto = Produto.builder()
                .qtdEstoque(estoque)
                .disponivelVenda(true)
                .ativo(true)
                .custo(custo)
                .valorVenda(valorVenda)
                .build();
        produto.setId(id);
        produto.setOrganizationId(1);
        return produto;
    }

    private Servico servicoDisponivel(int id, boolean disponivelVenda) {
        Servico servico = Servico.builder()
                .disponivelVenda(disponivelVenda)
                .ativo(true)
                .custo(BigDecimal.valueOf(15))
                .valorVenda(BigDecimal.valueOf(45))
                .tempoExecucao(60)
                .build();
        servico.setId(id);
        servico.setOrganizationId(1);
        servico.setSequencialUsuario(1);
        return servico;
    }

    private Cliente clienteAtivo(BigDecimal churn, BigDecimal averageTicket) {
        Cliente cliente = Cliente.builder()
                .ativo(true)
                .churnScore(churn != null ? churn : BigDecimal.ZERO)
                .averageTicket(averageTicket != null ? averageTicket : BigDecimal.ZERO)
                .build();
        cliente.setId(1);
        cliente.setOrganizationId(1);
        return cliente;
    }
}
