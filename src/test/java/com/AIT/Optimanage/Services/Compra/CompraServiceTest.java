package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Mappers.CompraMapper;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraResponseDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraProdutoDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraServicoDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Repositories.Compra.CompraProdutoRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraServicoRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.InventoryService;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorMetricsService;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Services.Compra.PagamentoCompraService;
import com.AIT.Optimanage.Validation.AgendaValidator;
import com.AIT.Optimanage.Validation.CompraValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompraServiceTest {

    @Mock private CompraRepository compraRepository;
    @Mock private FornecedorService fornecedorService;
    @Mock private ContadorService contadorService;
    @Mock private ProdutoService produtoService;
    @Mock private ServicoService servicoService;
    @Mock private CompraProdutoRepository compraProdutoRepository;
    @Mock private CompraServicoRepository compraServicoRepository;
    @Mock private PagamentoCompraService pagamentoCompraService;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private CompraMapper compraMapper;
    @Mock private InventoryService inventoryService;
    @Mock private FornecedorMetricsService fornecedorMetricsService;
    @Mock private CompraValidator compraValidator;
    @Mock private PlanoService planoService;
    @Mock private AgendaValidator agendaValidator;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private CompraService compraService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder().build();
        user.setId(1);
        user.setTenantId(1);
        CurrentUser.set(user);

        Plano plano = new Plano();
        plano.setAgendaHabilitada(true);
        plano.setPagamentosHabilitados(true);
        lenient().when(planoService.obterPlanoUsuario(user)).thenReturn(Optional.of(plano));

        lenient().when(agendaValidator.validarDataAgendamento(anyString()))
                .thenAnswer(invocation -> LocalDate.parse(invocation.getArgument(0)));
        lenient().when(agendaValidator.validarHoraAgendada(anyString()))
                .thenAnswer(invocation -> LocalTime.parse(invocation.getArgument(0)));
        lenient().when(agendaValidator.validarDuracao(any()))
                .thenAnswer(invocation -> {
                    Integer minutos = invocation.getArgument(0);
                    if (minutos == null) {
                        return Duration.ofHours(1);
                    }
                    return Duration.ofMinutes(minutos.longValue());
                });
        doNothing().when(agendaValidator)
                .validarConflitosAgendamentoCompra(any(), any(), any(), any(), any());
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void agendarCompraAtualizaStatusEData() {
        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.valueOf(100))
                .status(StatusCompra.AGUARDANDO_EXECUCAO)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(10);
        compra.setTenantId(1);

        when(compraRepository.findByIdAndOrganizationId(10, 1)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.agendarCompra(10, "2024-05-01", "10:00", 60);

        assertEquals(StatusCompra.AGENDADA, compra.getStatus());
        assertEquals(LocalDate.parse("2024-05-01"), compra.getDataAgendada());
    }

    @Test
    void finalizarAgendamentoCompraSemPagamentoDefineStatusAguardandoPag() {
        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.valueOf(100))
                .status(StatusCompra.AGENDADA)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(11);
        compra.setTenantId(1);

        when(compraRepository.findByIdAndOrganizationId(11, 1)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.finalizarAgendamentoCompra(11);

        assertEquals(StatusCompra.AGUARDANDO_PAG, compra.getStatus());
    }

    @Test
    void finalizarAgendamentoCompraComPagamentoCompletoDefineStatusConcretizado() {
        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.ZERO)
                .status(StatusCompra.AGENDADA)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(12);
        compra.setTenantId(1);

        when(compraRepository.findByIdAndOrganizationId(12, 1)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.finalizarAgendamentoCompra(12);

        assertEquals(StatusCompra.CONCRETIZADO, compra.getStatus());
    }

    @Test
    void editarCompraAtualizaItensTotaisEPendenciasConsiderandoValorNegociado() {
        Produto produtoAntigo = Produto.builder()
                .valorVenda(BigDecimal.TEN)
                .custo(BigDecimal.valueOf(8))
                .build();
        produtoAntigo.setId(8);

        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now().minusDays(5))
                .valorFinal(BigDecimal.valueOf(50))
                .valorPendente(BigDecimal.valueOf(20))
                .status(StatusCompra.AGUARDANDO_PAG)
                .compraProdutos(Collections.singletonList(CompraProduto.builder()
                        .produto(produtoAntigo)
                        .quantidade(1)
                        .valorUnitario(produtoAntigo.getCusto())
                        .valorTotal(BigDecimal.TEN)
                        .build()))
                .compraServicos(Collections.emptyList())
                .pagamentos(Collections.singletonList(CompraPagamento.builder()
                        .valorPago(BigDecimal.valueOf(80))
                        .dataVencimento(LocalDate.now())
                        .formaPagamento(FormaPagamento.PIX)
                        .statusPagamento(StatusPagamento.PAGO)
                        .build()))
                .build();
        compra.setId(15);
        compra.setTenantId(1);

        Produto produtoAtualizado = Produto.builder()
                .valorVenda(BigDecimal.valueOf(100))
                .custo(BigDecimal.valueOf(70))
                .build();
        produtoAtualizado.setId(5);

        Servico servicoAtualizado = Servico.builder()
                .custo(BigDecimal.valueOf(45))
                .valorVenda(BigDecimal.valueOf(50))
                .build();
        servicoAtualizado.setId(3);

        BigDecimal custoNegociado = BigDecimal.valueOf(75);
        CompraDTO compraDTO = CompraDTO.builder()
                .fornecedorId(1)
                .dataEfetuacao(LocalDate.now())
                .dataAgendada(null)
                .condicaoPagamento("30 dias")
                .status(StatusCompra.AGUARDANDO_PAG)
                .observacoes("Atualizado")
                .produtos(Collections.singletonList(new CompraProdutoDTO(produtoAtualizado.getId(), 2, custoNegociado)))
                .servicos(Collections.singletonList(new CompraServicoDTO(servicoAtualizado.getId(), 1)))
                .build();

        when(compraRepository.findByIdAndOrganizationId(15, 1)).thenReturn(Optional.of(compra));
        when(produtoService.buscarProdutoAtivo(produtoAtualizado.getId())).thenReturn(produtoAtualizado);
        when(servicoService.buscarServicoAtivo(servicoAtualizado.getId())).thenReturn(servicoAtualizado);
        when(compraRepository.save(compra)).thenReturn(compra);
        CompraResponseDTO expectedResponse = new CompraResponseDTO();
        when(compraMapper.toResponse(compra)).thenReturn(expectedResponse);

        CompraResponseDTO response = compraService.editarCompra(15, compraDTO);

        CompraProduto compraProdutoAtualizado = compra.getCompraProdutos().get(0);
        assertEquals(custoNegociado, compraProdutoAtualizado.getValorUnitario());
        assertEquals(custoNegociado.multiply(BigDecimal.valueOf(2)), compraProdutoAtualizado.getValorTotal());
        assertEquals(BigDecimal.valueOf(195), compra.getValorFinal());
        assertEquals(BigDecimal.valueOf(115), compra.getValorPendente());
        assertEquals(compraDTO.getDataEfetuacao(), compra.getDataEfetuacao());
        assertEquals("Atualizado", compra.getObservacoes());
        assertEquals(1, compra.getCompraProdutos().size());
        assertEquals(1, compra.getCompraServicos().size());
        assertEquals(servicoAtualizado.getCusto(), compra.getCompraServicos().get(0).getValorUnitario());
        assertEquals(servicoAtualizado.getCusto().multiply(BigDecimal.valueOf(1)),
                compra.getCompraServicos().get(0).getValorTotal());
        verify(compraProdutoRepository).saveAll(anyList());
        verify(compraServicoRepository).saveAll(anyList());
        verify(inventoryService).reduzir(produtoAntigo.getId(), 1, InventorySource.COMPRA, compra.getId(),
                "Ajuste de edição da compra #" + compra.getId());
        verify(inventoryService).incrementar(produtoAtualizado.getId(), 2, InventorySource.COMPRA, compra.getId(),
                "Atualização da compra #" + compra.getId());
        assertEquals(expectedResponse, response);
    }

    @Test
    void editarCompraComPagamentosSuperioresAoTotalZeraValorPendente() {
        Produto produtoAntigo = Produto.builder()
                .valorVenda(BigDecimal.valueOf(60))
                .custo(BigDecimal.valueOf(40))
                .build();
        produtoAntigo.setId(41);

        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now().minusDays(2))
                .valorFinal(BigDecimal.valueOf(200))
                .valorPendente(BigDecimal.valueOf(50))
                .status(StatusCompra.AGUARDANDO_PAG)
                .compraProdutos(Collections.singletonList(CompraProduto.builder()
                        .produto(produtoAntigo)
                        .quantidade(1)
                        .valorUnitario(produtoAntigo.getCusto())
                        .valorTotal(produtoAntigo.getCusto())
                        .build()))
                .compraServicos(Collections.emptyList())
                .pagamentos(Collections.singletonList(CompraPagamento.builder()
                        .valorPago(BigDecimal.valueOf(150))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build()))
                .build();
        compra.setId(31);
        compra.setTenantId(1);

        Produto produtoAtualizado = Produto.builder()
                .valorVenda(BigDecimal.valueOf(30))
                .custo(BigDecimal.valueOf(30))
                .build();
        produtoAtualizado.setId(42);

        CompraDTO compraDTO = CompraDTO.builder()
                .fornecedorId(1)
                .dataEfetuacao(LocalDate.now())
                .condicaoPagamento("30 dias")
                .status(StatusCompra.AGUARDANDO_PAG)
                .produtos(Collections.singletonList(new CompraProdutoDTO(produtoAtualizado.getId(), 1,
                        produtoAtualizado.getCusto())))
                .servicos(Collections.emptyList())
                .build();

        when(compraRepository.findByIdAndOrganizationId(31, 1)).thenReturn(Optional.of(compra));
        when(produtoService.buscarProdutoAtivo(produtoAtualizado.getId())).thenReturn(produtoAtualizado);
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.editarCompra(31, compraDTO);

        assertEquals(BigDecimal.ZERO, compra.getValorPendente());
    }

    @Test
    void editarCompraSemValorUnitarioUsaCustoDoProduto() {
        Produto produtoAntigo = Produto.builder()
                .valorVenda(BigDecimal.valueOf(30))
                .custo(BigDecimal.valueOf(20))
                .build();
        produtoAntigo.setId(18);

        Compra compra = Compra.builder()
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now().minusDays(3))
                .valorFinal(BigDecimal.valueOf(20))
                .valorPendente(BigDecimal.valueOf(20))
                .status(StatusCompra.AGUARDANDO_PAG)
                .compraProdutos(Collections.singletonList(CompraProduto.builder()
                        .produto(produtoAntigo)
                        .quantidade(1)
                        .valorUnitario(produtoAntigo.getCusto())
                        .valorTotal(produtoAntigo.getCusto())
                        .build()))
                .compraServicos(Collections.emptyList())
                .pagamentos(null)
                .build();
        compra.setId(25);
        compra.setTenantId(1);

        Produto produtoAtualizado = Produto.builder()
                .valorVenda(BigDecimal.valueOf(60))
                .custo(BigDecimal.valueOf(40))
                .build();
        produtoAtualizado.setId(30);

        CompraProdutoDTO produtoDTO = new CompraProdutoDTO();
        produtoDTO.setProdutoId(produtoAtualizado.getId());
        produtoDTO.setQuantidade(3);

        CompraDTO compraDTO = CompraDTO.builder()
                .fornecedorId(1)
                .dataEfetuacao(LocalDate.now())
                .dataAgendada(null)
                .condicaoPagamento("À vista")
                .status(StatusCompra.AGUARDANDO_PAG)
                .observacoes("Sem custo informado")
                .produtos(Collections.singletonList(produtoDTO))
                .servicos(Collections.emptyList())
                .build();

        when(compraRepository.findByIdAndOrganizationId(25, 1)).thenReturn(Optional.of(compra));
        when(produtoService.buscarProdutoAtivo(produtoAtualizado.getId())).thenReturn(produtoAtualizado);
        when(compraRepository.save(compra)).thenReturn(compra);
        CompraResponseDTO expectedResponse = new CompraResponseDTO();
        when(compraMapper.toResponse(compra)).thenReturn(expectedResponse);

        CompraResponseDTO response = compraService.editarCompra(25, compraDTO);

        CompraProduto produtoSalvo = compra.getCompraProdutos().get(0);
        assertEquals(produtoAtualizado.getCusto(), produtoSalvo.getValorUnitario());
        assertEquals(produtoAtualizado.getCusto().multiply(BigDecimal.valueOf(3)), produtoSalvo.getValorTotal());
        assertEquals(produtoSalvo.getValorTotal(), compra.getValorFinal());
        assertEquals(compra.getValorFinal(), compra.getValorPendente());
        verify(compraProdutoRepository).saveAll(anyList());
        verify(compraServicoRepository).saveAll(anyList());
        verify(inventoryService).reduzir(produtoAntigo.getId(), 1, InventorySource.COMPRA, compra.getId(),
                "Ajuste de edição da compra #" + compra.getId());
        verify(inventoryService).incrementar(produtoAtualizado.getId(), 3, InventorySource.COMPRA, compra.getId(),
                "Atualização da compra #" + compra.getId());
        assertEquals(expectedResponse, response);
    }

    @Test
    void criarCompraComConflitoDeAgendaParaMesmoUsuarioELancaExcecao() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(5);

        Contador contador = new Contador();
        contador.setContagemAtual(1);

        Produto produto = Produto.builder()
                .custo(BigDecimal.valueOf(25))
                .valorVenda(BigDecimal.valueOf(30))
                .build();
        produto.setId(9);

        Servico servico = Servico.builder()
                .custo(BigDecimal.valueOf(40))
                .valorVenda(BigDecimal.valueOf(50))
                .build();
        servico.setId(4);

        when(fornecedorService.listarUmFornecedor(fornecedor.getId())).thenReturn(fornecedor);
        when(contadorService.BuscarContador(Tabela.COMPRA)).thenReturn(contador);
        when(produtoService.buscarProdutoAtivo(produto.getId())).thenReturn(produto);
        when(servicoService.buscarServicoAtivo(servico.getId())).thenReturn(servico);
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            if (compra.getId() == null) {
                compra.setId(1);
            }
            return compra;
        });
        when(compraMapper.toResponse(any(Compra.class))).thenReturn(new CompraResponseDTO());

        CompraDTO compraDTO = CompraDTO.builder()
                .fornecedorId(fornecedor.getId())
                .dataEfetuacao(LocalDate.now())
                .dataAgendada(LocalDate.now().plusDays(1))
                .horaAgendada(LocalTime.of(10, 0))
                .duracaoEstimada(Duration.ofMinutes(60))
                .status(StatusCompra.AGUARDANDO_EXECUCAO)
                .produtos(Collections.singletonList(new CompraProdutoDTO(produto.getId(), 1, produto.getCusto())))
                .servicos(Collections.singletonList(new CompraServicoDTO(servico.getId(), 1)))
                .build();

        compraService.criarCompra(compraDTO);

        doThrow(new IllegalArgumentException("Já existe um agendamento para este usuário no período informado."))
                .when(agendaValidator)
                .validarConflitosAgendamentoCompra(any(Compra.class), any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class, () -> compraService.criarCompra(compraDTO));
        verify(compraRepository, times(1)).save(any(Compra.class));
    }
}

