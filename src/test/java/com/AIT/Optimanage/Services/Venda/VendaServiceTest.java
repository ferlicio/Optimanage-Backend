package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaResponseDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaServicoRepository;
import com.AIT.Optimanage.Services.Cliente.ClienteService;
import com.AIT.Optimanage.Services.InventoryService;
import com.AIT.Optimanage.Services.Venda.PagamentoVendaService;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Validation.VendaValidator;
import com.AIT.Optimanage.Mappers.VendaMapper;
import com.AIT.Optimanage.Payments.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private ServicoService servicoService;
    @Mock
    private PlanoService planoService;
    @Mock
    private VendaProdutoRepository vendaProdutoRepository;
    @Mock
    private VendaServicoRepository vendaServicoRepository;
    @Mock
    private ContadorService contadorService;
    @Mock
    private PagamentoVendaService pagamentoVendaService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentConfigService paymentConfigService;
    @Mock
    private VendaMapper vendaMapper;
    @Mock
    private VendaValidator vendaValidator;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VendaService vendaService;

    @Test
    void atualizarVendaAtualizaItensDescontoEEstoque() {
        LocalDate hoje = LocalDate.now();

        User loggedUser = new User();
        loggedUser.setTenantId(10);

        Venda venda = new Venda();
        venda.setId(55);
        venda.setTenantId(10);
        venda.setCliente(new Cliente());
        venda.setSequencialUsuario(1);
        venda.setDataEfetuacao(hoje.minusDays(3));
        venda.setDataAgendada(null);
        venda.setDataCobranca(hoje.minusDays(1));
        venda.setValorTotal(new BigDecimal("100.00"));
        venda.setDescontoGeral(BigDecimal.ZERO);
        venda.setValorFinal(new BigDecimal("80.00"));
        venda.setValorPendente(new BigDecimal("30.00"));
        venda.setStatus(StatusVenda.PENDENTE);

        Produto produtoAntigo = new Produto();
        produtoAntigo.setId(1);
        produtoAntigo.setValorVenda(new BigDecimal("40.00"));
        VendaProduto vendaProdutoAntigo = new VendaProduto();
        vendaProdutoAntigo.setVenda(venda);
        vendaProdutoAntigo.setProduto(produtoAntigo);
        vendaProdutoAntigo.setQuantidade(1);
        vendaProdutoAntigo.setValorFinal(new BigDecimal("40.00"));

        venda.setVendaProdutos(List.of(vendaProdutoAntigo));

        Servico servicoAntigo = new Servico();
        servicoAntigo.setId(2);
        servicoAntigo.setValorVenda(new BigDecimal("20.00"));
        VendaServico vendaServicoAntigo = new VendaServico();
        vendaServicoAntigo.setVenda(venda);
        vendaServicoAntigo.setServico(servicoAntigo);
        vendaServicoAntigo.setQuantidade(1);
        vendaServicoAntigo.setValorFinal(new BigDecimal("20.00"));
        venda.setVendaServicos(List.of(vendaServicoAntigo));

        VendaPagamento pagamento = new VendaPagamento();
        pagamento.setValorPago(new BigDecimal("50.00"));
        pagamento.setStatusPagamento(StatusPagamento.PAGO);
        pagamento.setDataVencimento(hoje.minusDays(1));
        pagamento.setFormaPagamento(FormaPagamento.DINHEIRO);
        venda.setPagamentos(List.of(pagamento));

        when(vendaRepository.findByIdAndOrganizationId(eq(venda.getId()), eq(loggedUser.getTenantId())))
                .thenReturn(Optional.of(venda));
        when(produtoService.buscarProdutoAtivo(3)).thenReturn(criarProdutoNovo());
        when(servicoService.buscarServicoAtivo(4)).thenReturn(criarServicoNovo());
        when(vendaRepository.save(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vendaMapper.toResponse(any(Venda.class))).thenReturn(new VendaResponseDTO());

        doNothing().when(vendaValidator).validarVenda(any(VendaDTO.class), any(User.class));
        doNothing().when(inventoryService).incrementar(anyInt(), anyInt(), any(InventorySource.class), anyInt(), anyString());
        doNothing().when(inventoryService).reduzir(anyInt(), anyInt(), any(InventorySource.class), anyInt(), anyString());
        doNothing().when(vendaProdutoRepository).deleteByVenda(any(Venda.class));
        doNothing().when(vendaServicoRepository).deleteByVenda(any(Venda.class));

        VendaProdutoDTO novoProdutoDTO = new VendaProdutoDTO(3, 2, new BigDecimal("33.333"));
        VendaServicoDTO novoServicoDTO = new VendaServicoDTO(4, 1, BigDecimal.ZERO);

        VendaDTO vendaDTO = VendaDTO.builder()
                .clienteId(5)
                .dataEfetuacao(hoje)
                .dataAgendada(null)
                .dataCobranca(hoje)
                .descontoGeral(null)
                .condicaoPagamento("Cartão")
                .alteracoesPermitidas(2)
                .status(StatusVenda.AGUARDANDO_PAG)
                .observacoes("Atualizada")
                .produtos(List.of(novoProdutoDTO))
                .servicos(List.of(novoServicoDTO))
                .build();

        vendaService.atualizarVenda(loggedUser, venda.getId(), vendaDTO);

        assertEquals(hoje, venda.getDataEfetuacao());
        assertEquals("Cartão", venda.getCondicaoPagamento());
        assertEquals(2, venda.getAlteracoesPermitidas());
        assertEquals("Atualizada", venda.getObservacoes());
        assertNotNull(venda.getVendaProdutos());
        assertEquals(1, venda.getVendaProdutos().size());
        VendaProduto novoProduto = venda.getVendaProdutos().get(0);
        assertSame(venda, novoProduto.getVenda());
        assertNotNull(venda.getVendaServicos());
        assertEquals(1, venda.getVendaServicos().size());
        VendaServico novoServico = venda.getVendaServicos().get(0);
        assertSame(venda, novoServico.getVenda());
        assertEquals(0, new BigDecimal("50.00").compareTo(novoServico.getValorFinal()));
        assertEquals(0, new BigDecimal("133.33").compareTo(venda.getValorPendente()));
        assertEquals(0, new BigDecimal("183.33").compareTo(venda.getValorTotal()));
        assertEquals(0, new BigDecimal("183.33").compareTo(venda.getValorFinal()));
        assertEquals(StatusVenda.AGUARDANDO_PAG, venda.getStatus());
        assertEquals(BigDecimal.ZERO, venda.getDescontoGeral());

        verify(vendaProdutoRepository).deleteByVenda(venda);
        verify(vendaServicoRepository).deleteByVenda(venda);
        verify(inventoryService).incrementar(eq(produtoAntigo.getId()), eq(1), eq(InventorySource.VENDA), eq(venda.getId()), contains("Reversão"));
        verify(inventoryService).reduzir(eq(3), eq(2), eq(InventorySource.VENDA), eq(venda.getId()), contains("Atualização"));

        verify(vendaServicoRepository).saveAll(any());

        ArgumentCaptor<List<VendaProduto>> produtosCaptor = ArgumentCaptor.forClass(List.class);
        verify(vendaProdutoRepository).saveAll(produtosCaptor.capture());
        assertEquals(1, produtosCaptor.getValue().size());
        VendaProduto produtoCapturado = produtosCaptor.getValue().get(0);
        assertSame(venda, produtoCapturado.getVenda());
        assertEquals(0, new BigDecimal("133.33").compareTo(produtoCapturado.getValorFinal()));
    }

    private Produto criarProdutoNovo() {
        Produto produto = new Produto();
        produto.setId(3);
        produto.setValorVenda(new BigDecimal("100.00"));
        return produto;
    }

    private Servico criarServicoNovo() {
        Servico servico = new Servico();
        servico.setId(4);
        servico.setValorVenda(new BigDecimal("50.00"));
        return servico;
    }
}
