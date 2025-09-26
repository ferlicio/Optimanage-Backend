package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Plano;
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
import com.AIT.Optimanage.Services.AuditTrailService;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Validation.VendaValidator;
import com.AIT.Optimanage.Validation.AgendaValidator;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private AuditTrailService auditTrailService;
    @Mock
    private AgendaValidator agendaValidator;
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

        when(vendaRepository.findDetailedByIdAndOrganizationId(eq(venda.getId()), eq(loggedUser.getTenantId())))
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

    @Test
    void atualizarVendaComPagamentosSuperioresAoTotalNaoGeraSaldoNegativo() {
        LocalDate hoje = LocalDate.now();

        User loggedUser = new User();
        loggedUser.setTenantId(12);

        Venda venda = new Venda();
        venda.setId(88);
        venda.setTenantId(12);
        venda.setStatus(StatusVenda.PENDENTE);
        venda.setValorTotal(new BigDecimal("180.00"));
        venda.setDescontoGeral(BigDecimal.ZERO);
        venda.setValorFinal(new BigDecimal("180.00"));
        venda.setValorPendente(new BigDecimal("20.00"));

        Produto produtoAntigo = new Produto();
        produtoAntigo.setId(15);
        produtoAntigo.setValorVenda(new BigDecimal("90.00"));
        VendaProduto vendaProdutoAntigo = new VendaProduto();
        vendaProdutoAntigo.setVenda(venda);
        vendaProdutoAntigo.setProduto(produtoAntigo);
        vendaProdutoAntigo.setQuantidade(1);
        vendaProdutoAntigo.setValorFinal(new BigDecimal("90.00"));
        venda.setVendaProdutos(List.of(vendaProdutoAntigo));
        venda.setVendaServicos(List.of());

        VendaPagamento pagamento = new VendaPagamento();
        pagamento.setValorPago(new BigDecimal("150.00"));
        pagamento.setStatusPagamento(StatusPagamento.PAGO);
        venda.setPagamentos(List.of(pagamento));

        Produto novoProduto = new Produto();
        novoProduto.setId(16);
        novoProduto.setValorVenda(new BigDecimal("30.00"));

        VendaProdutoDTO novoProdutoDTO = new VendaProdutoDTO(novoProduto.getId(), 1, BigDecimal.ZERO);

        VendaDTO vendaDTO = VendaDTO.builder()
                .clienteId(5)
                .dataEfetuacao(hoje)
                .status(StatusVenda.AGUARDANDO_PAG)
                .produtos(List.of(novoProdutoDTO))
                .servicos(List.of())
                .build();

        when(vendaRepository.findDetailedByIdAndOrganizationId(venda.getId(), loggedUser.getTenantId()))
                .thenReturn(Optional.of(venda));
        when(produtoService.buscarProdutoAtivo(novoProduto.getId())).thenReturn(novoProduto);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vendaMapper.toResponse(any(Venda.class))).thenReturn(new VendaResponseDTO());

        doNothing().when(vendaValidator).validarVenda(any(VendaDTO.class), any(User.class));
        doNothing().when(inventoryService).incrementar(anyInt(), anyInt(), any(InventorySource.class), anyInt(), anyString());
        doNothing().when(inventoryService).reduzir(anyInt(), anyInt(), any(InventorySource.class), anyInt(), anyString());
        doNothing().when(vendaProdutoRepository).deleteByVenda(any(Venda.class));
        doNothing().when(vendaServicoRepository).deleteByVenda(any(Venda.class));

        vendaService.atualizarVenda(loggedUser, venda.getId(), vendaDTO);

        assertEquals(0, venda.getValorPendente().compareTo(BigDecimal.ZERO));
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

    @Test
    void estornarPagamentoVendaAtualizaSaldoEStatusParaParcialQuandoHaPagamentosRestantes() {
        User loggedUser = new User();
        loggedUser.setTenantId(5);

        Plano plano = new Plano();
        plano.setPagamentosHabilitados(true);

        Venda venda = new Venda();
        venda.setId(11);
        venda.setTenantId(5);
        venda.setValorFinal(new BigDecimal("100.00"));
        venda.setValorPendente(BigDecimal.ZERO);
        venda.setStatus(StatusVenda.PAGA);

        VendaPagamento pagamentoEstornado = new VendaPagamento();
        pagamentoEstornado.setId(1);
        pagamentoEstornado.setValorPago(new BigDecimal("60.00"));
        pagamentoEstornado.setStatusPagamento(StatusPagamento.PAGO);

        VendaPagamento pagamentoRemanescente = new VendaPagamento();
        pagamentoRemanescente.setId(2);
        pagamentoRemanescente.setValorPago(new BigDecimal("40.00"));
        pagamentoRemanescente.setStatusPagamento(StatusPagamento.PAGO);

        venda.setPagamentos(List.of(pagamentoEstornado, pagamentoRemanescente));

        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));
        when(vendaRepository.findDetailedByIdAndOrganizationId(venda.getId(), loggedUser.getTenantId())).thenReturn(Optional.of(venda));
        when(pagamentoVendaService.listarUmPagamento(loggedUser, pagamentoEstornado.getId())).thenReturn(pagamentoEstornado);
        when(pagamentoVendaService.listarPagamentosRealizadosVenda(loggedUser, venda.getId()))
                .thenReturn(List.of(pagamentoRemanescente));
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(vendaMapper.toResponse(venda)).thenReturn(new VendaResponseDTO());

        VendaResponseDTO responseDTO = vendaService.estornarPagamentoVenda(loggedUser, venda.getId(), pagamentoEstornado.getId());

        assertNotNull(responseDTO);
        assertEquals(new BigDecimal("60.00"), venda.getValorPendente());
        assertEquals(StatusVenda.PARCIALMENTE_PAGA, venda.getStatus());
        verify(pagamentoVendaService).estornarPagamento(loggedUser, pagamentoEstornado);
        verify(pagamentoVendaService).listarPagamentosRealizadosVenda(loggedUser, venda.getId());
    }

    @Test
    void estornarPagamentoVendaAtualizaSaldoEStatusParaAguardandoQuandoNaoHaPagamentos() {
        User loggedUser = new User();
        loggedUser.setTenantId(6);

        Plano plano = new Plano();
        plano.setPagamentosHabilitados(true);

        Venda venda = new Venda();
        venda.setId(22);
        venda.setTenantId(6);
        venda.setValorFinal(new BigDecimal("150.00"));
        venda.setValorPendente(BigDecimal.ZERO);
        venda.setStatus(StatusVenda.PAGA);

        VendaPagamento pagamentoEstornado = new VendaPagamento();
        pagamentoEstornado.setId(3);
        pagamentoEstornado.setValorPago(new BigDecimal("150.00"));
        pagamentoEstornado.setStatusPagamento(StatusPagamento.PAGO);

        venda.setPagamentos(List.of(pagamentoEstornado));

        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));
        when(vendaRepository.findDetailedByIdAndOrganizationId(venda.getId(), loggedUser.getTenantId())).thenReturn(Optional.of(venda));
        when(pagamentoVendaService.listarUmPagamento(loggedUser, pagamentoEstornado.getId())).thenReturn(pagamentoEstornado);
        when(pagamentoVendaService.listarPagamentosRealizadosVenda(loggedUser, venda.getId())).thenReturn(List.of());
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(vendaMapper.toResponse(venda)).thenReturn(new VendaResponseDTO());

        VendaResponseDTO responseDTO = vendaService.estornarPagamentoVenda(loggedUser, venda.getId(), pagamentoEstornado.getId());

        assertNotNull(responseDTO);
        assertEquals(new BigDecimal("150.00"), venda.getValorPendente());
        assertEquals(StatusVenda.AGUARDANDO_PAG, venda.getStatus());
        verify(pagamentoVendaService).estornarPagamento(loggedUser, pagamentoEstornado);
        verify(pagamentoVendaService).listarPagamentosRealizadosVenda(loggedUser, venda.getId());
    }

    @Test
    void cancelarVendaDevolveEstoqueEZeraValorPendente() {
        User loggedUser = new User();
        loggedUser.setTenantId(8);

        Venda venda = new Venda();
        venda.setId(44);
        venda.setTenantId(8);
        venda.setValorFinal(new BigDecimal("120.00"));
        venda.setValorPendente(new BigDecimal("80.00"));
        venda.setStatus(StatusVenda.AGUARDANDO_PAG);

        Produto produto = new Produto();
        produto.setId(77);

        VendaProduto vendaProduto = new VendaProduto();
        vendaProduto.setProduto(produto);
        vendaProduto.setQuantidade(3);
        vendaProduto.setVenda(venda);
        venda.setVendaProdutos(List.of(vendaProduto));

        when(vendaRepository.findDetailedByIdAndOrganizationId(venda.getId(), loggedUser.getTenantId())).thenReturn(Optional.of(venda));
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(vendaMapper.toResponse(venda)).thenReturn(new VendaResponseDTO());

        VendaResponseDTO responseDTO = vendaService.cancelarVenda(loggedUser, venda.getId());

        assertNotNull(responseDTO);
        assertEquals(BigDecimal.ZERO, venda.getValorPendente());
        assertEquals(StatusVenda.CANCELADA, venda.getStatus());
        verify(inventoryService).incrementar(eq(produto.getId()), eq(3), eq(InventorySource.VENDA), eq(venda.getId()), contains("Cancelamento"));
        verify(vendaRepository).save(venda);
    }

    @Test
    void estornarVendaIntegralDevolveEstoqueEAtualizaStatusEValores() {
        User loggedUser = new User();
        loggedUser.setTenantId(9);

        Plano plano = new Plano();
        plano.setPagamentosHabilitados(true);

        Venda venda = new Venda();
        venda.setId(45);
        venda.setTenantId(9);
        venda.setValorFinal(new BigDecimal("250.00"));
        venda.setValorPendente(BigDecimal.ZERO);
        venda.setStatus(StatusVenda.PAGA);

        Produto produto = new Produto();
        produto.setId(88);

        VendaProduto vendaProduto = new VendaProduto();
        vendaProduto.setProduto(produto);
        vendaProduto.setQuantidade(2);
        vendaProduto.setVenda(venda);
        venda.setVendaProdutos(List.of(vendaProduto));

        VendaPagamento pagamento1 = new VendaPagamento();
        pagamento1.setStatusPagamento(StatusPagamento.PAGO);
        VendaPagamento pagamento2 = new VendaPagamento();
        pagamento2.setStatusPagamento(StatusPagamento.PAGO);
        venda.setPagamentos(List.of(pagamento1, pagamento2));

        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));
        when(vendaRepository.findDetailedByIdAndOrganizationId(venda.getId(), loggedUser.getTenantId())).thenReturn(Optional.of(venda));
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(vendaMapper.toResponse(venda)).thenReturn(new VendaResponseDTO());

        VendaResponseDTO responseDTO = vendaService.estornarVendaIntegral(loggedUser, venda.getId());

        assertNotNull(responseDTO);
        assertEquals(BigDecimal.ZERO, venda.getValorPendente());
        assertEquals(StatusVenda.CANCELADA, venda.getStatus());
        verify(inventoryService).incrementar(eq(produto.getId()), eq(2), eq(InventorySource.VENDA), eq(venda.getId()),
                contains("Estorno integral"));
        verify(pagamentoVendaService, times(2)).estornarPagamento(eq(loggedUser), any(VendaPagamento.class));
        verify(vendaRepository).save(venda);
    }
}
