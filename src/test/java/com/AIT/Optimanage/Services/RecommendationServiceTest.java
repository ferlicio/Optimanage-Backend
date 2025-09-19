package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private PlanoService planoService;
    @Mock
    private CompatibilidadeService compatibilidadeService;

    @InjectMocks
    private RecommendationService recommendationService;

    private User loggedUser;

    @BeforeEach
    void setUp() {
        loggedUser = new User();
        loggedUser.setTenantId(1);
        CurrentUser.set(loggedUser);

        Plano plano = new Plano();
        plano.setRecomendacoesHabilitadas(true);
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.of(plano));
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void recomendarProdutosIgnoraItensSemEstoque() {
        when(vendaRepository.findTopProdutosByCliente(123, 1)).thenReturn(List.of(new Object[]{100, 5L}));

        Venda vendaRecente = criarVenda(LocalDate.of(2024, 1, 10),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 2));
        Venda vendaAntiga = criarVenda(LocalDate.of(2023, 12, 1),
                criarVendaProduto(100, 1),
                criarVendaProduto(300, 1));

        when(vendaRepository.findAllWithProdutosByOrganization(1)).thenReturn(List.of(vendaRecente, vendaAntiga));

        Produto disponivel = produtoComEstoque(200, 5, BigDecimal.valueOf(15), BigDecimal.valueOf(30));
        Produto semEstoque = produtoComEstoque(300, 0, BigDecimal.valueOf(15), BigDecimal.valueOf(30));

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(disponivel, semEstoque));

        List<ProdutoResponse> recomendados = recommendationService.recomendarProdutos(123);

        assertEquals(1, recomendados.size());
        assertEquals(200, recomendados.get(0).getId());

        ArgumentCaptor<Collection<Integer>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(produtoRepository).findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(captor.capture(), eq(1));
        assertTrue(captor.getValue().contains(200));
        verify(compatibilidadeService, never()).buscarCompatibilidades(any(User.class), anyString());
    }

    @Test
    void recomendarProdutosPriorizaItensCompativeis() {
        when(vendaRepository.findTopProdutosByCliente(321, 1)).thenReturn(List.of(new Object[]{100, 3L}));

        Venda venda = criarVenda(LocalDate.of(2024, 1, 10),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 1),
                criarVendaProduto(300, 1));

        when(vendaRepository.findAllWithProdutosByOrganization(1)).thenReturn(List.of(venda));

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

        List<ProdutoResponse> recomendados = recommendationService.recomendarProdutos(321, "Contexto A");

        assertEquals(2, recomendados.size());
        assertEquals(300, recomendados.get(0).getId());
    }

    @Test
    void recomendarProdutosLimitaQuantidadeDeSugestoes() {
        when(vendaRepository.findTopProdutosByCliente(999, 1)).thenReturn(List.of(new Object[]{100, 1L}));

        List<VendaProduto> itens = new ArrayList<>();
        itens.add(criarVendaProduto(100, 1));
        for (int i = 0; i < 12; i++) {
            itens.add(criarVendaProduto(200 + i, 1));
        }
        Venda venda = criarVenda(LocalDate.of(2024, 1, 1), itens.toArray(new VendaProduto[0]));

        when(vendaRepository.findAllWithProdutosByOrganization(1)).thenReturn(List.of(venda));

        List<Produto> produtos = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            produtos.add(produtoComEstoque(200 + i, 2, BigDecimal.valueOf(5), BigDecimal.valueOf(12)));
        }

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(produtos);

        List<ProdutoResponse> recomendados = recommendationService.recomendarProdutos(999);

        assertEquals(10, recomendados.size());
        assertTrue(recomendados.stream().map(ProdutoResponse::getQtdEstoque).allMatch(qtd -> qtd != null && qtd > 0));
    }

    @Test
    void recomendarProdutosPriorizaMaiorMargemERecorrencia() {
        when(vendaRepository.findTopProdutosByCliente(555, 1)).thenReturn(List.of(new Object[]{100, 4L}));

        Venda venda = criarVenda(LocalDate.of(2024, 2, 1),
                criarVendaProduto(100, 1),
                criarVendaProduto(200, 3),
                criarVendaProduto(300, 1));

        when(vendaRepository.findAllWithProdutosByOrganization(1)).thenReturn(List.of(venda));

        Produto margemAlta = produtoComEstoque(200, 5, BigDecimal.valueOf(10), BigDecimal.valueOf(40));
        Produto recorrente = produtoComEstoque(300, 5, BigDecimal.valueOf(15), BigDecimal.valueOf(25));

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(margemAlta, recorrente));

        List<ProdutoResponse> recomendados = recommendationService.recomendarProdutos(555, null, false);

        assertEquals(2, recomendados.size());
        assertEquals(200, recomendados.get(0).getId());
    }

    @Test
    void recomendarProdutosRespeitaPlano() {
        Plano plano = new Plano();
        plano.setRecomendacoesHabilitadas(false);
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.of(plano));

        assertThrows(AccessDeniedException.class, () -> recommendationService.recomendarProdutos(1));
    }

    @Test
    void recomendarProdutosFuncionaSemCliente() {
        when(vendaRepository.findTopProdutosByOrganization(1)).thenReturn(List.of(new Object[]{400, 6L}));

        Venda venda = criarVenda(LocalDate.of(2024, 3, 10),
                criarVendaProduto(400, 2),
                criarVendaProduto(500, 1));

        when(vendaRepository.findAllWithProdutosByOrganization(1)).thenReturn(List.of(venda));

        Produto candidatoA = produtoComEstoque(400, 2, BigDecimal.valueOf(10), BigDecimal.valueOf(30));
        Produto candidatoB = produtoComEstoque(500, 2, BigDecimal.valueOf(5), BigDecimal.valueOf(35));

        when(produtoRepository.findAllByIdInAndOrganizationIdAndAtivoTrueAndDisponivelVendaTrue(anyCollection(), eq(1)))
                .thenReturn(List.of(candidatoA, candidatoB));

        List<ProdutoResponse> recomendados = recommendationService.recomendarProdutos(null, null, true);

        assertEquals(2, recomendados.size());
        assertTrue(recomendados.stream().anyMatch(produto -> produto.getId() == 500));
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
        Produto produto = Produto.builder().id(produtoId).custo(BigDecimal.TEN).valorVenda(BigDecimal.valueOf(20)).build();
        VendaProduto vendaProduto = new VendaProduto();
        vendaProduto.setProduto(produto);
        vendaProduto.setQuantidade(quantidade);
        return vendaProduto;
    }

    private Produto produtoComEstoque(int id, int estoque, BigDecimal custo, BigDecimal valorVenda) {
        return Produto.builder()
                .id(id)
                .organizationId(1)
                .qtdEstoque(estoque)
                .disponivelVenda(true)
                .ativo(true)
                .custo(custo)
                .valorVenda(valorVenda)
                .build();
    }
}

