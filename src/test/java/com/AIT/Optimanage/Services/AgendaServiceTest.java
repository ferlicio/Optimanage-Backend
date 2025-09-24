package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Agenda.AgendaSearch;
import com.AIT.Optimanage.Models.Agenda.EventoAgenda;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Enums.TipoEvento;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.Cliente.ClienteContatoRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Compra.PagamentoCompraRepository;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock private PagamentoCompraRepository pagamentoCompraRepository;
    @Mock private PagamentoVendaRepository pagamentoVendaRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private VendaRepository vendaRepository;
    @Mock private ClienteContatoRepository clienteContatoRepository;
    @Mock private PlanoService planoService;

    @InjectMocks private AgendaService agendaService;

    private User usuario;
    private Plano planoHabilitado;

    @BeforeEach
    void setUp() {
        usuario = User.builder().build();
        usuario.setId(5);
        usuario.setTenantId(42);

        planoHabilitado = Plano.builder().agendaHabilitada(true).build();
        when(planoService.obterPlanoUsuario(usuario)).thenReturn(Optional.of(planoHabilitado));
    }

    @Test
    void listarEventosIncluiPagamentosAgendamentosEAniversarios() {
        LocalDate inicio = LocalDate.of(2024, 1, 10);
        LocalDate fim = inicio.plusDays(5);

        AgendaSearch pesquisa = AgendaSearch.builder()
                .page(0)
                .pageSize(10)
                .dataInicial(inicio)
                .dataFinal(fim)
                .build();

        Compra compra = Compra.builder()
                .sequencialUsuario(120)
                .dataAgendada(inicio.plusDays(3))
                .observacoes("Reunião com fornecedor")
                .build();
        compra.setId(7);

        Venda venda = Venda.builder()
                .sequencialUsuario(220)
                .dataAgendada(inicio.plusDays(4))
                .observacoes("Apresentação para cliente")
                .build();
        venda.setId(8);

        CompraPagamento pagamentoCompra = CompraPagamento.builder()
                .dataVencimento(inicio.plusDays(1))
                .valorPago(BigDecimal.ZERO)
                .statusPagamento(StatusPagamento.PENDENTE)
                .compra(compra)
                .build();
        pagamentoCompra.setId(11);

        VendaPagamento pagamentoVenda = VendaPagamento.builder()
                .dataVencimento(inicio.plusDays(2))
                .valorPago(BigDecimal.ZERO)
                .statusPagamento(StatusPagamento.PENDENTE)
                .venda(venda)
                .build();
        pagamentoVenda.setId(22);

        ClienteContato contato = ClienteContato.builder()
                .nome("Maria Silva")
                .aniversario("15/01")
                .build();
        contato.setId(33);

        when(pagamentoCompraRepository.findAllByCompraOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(
                eq(usuario.getTenantId()), eq(StatusPagamento.PENDENTE), eq(inicio)))
                .thenReturn(List.of(pagamentoCompra));
        when(pagamentoVendaRepository.findAllByVendaOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(
                eq(usuario.getTenantId()), eq(StatusPagamento.PENDENTE), eq(inicio)))
                .thenReturn(List.of(pagamentoVenda));
        when(compraRepository.findAgendadasNoPeriodo(usuario.getTenantId(), usuario.getId(), inicio, fim))
                .thenReturn(List.of(compra));
        when(vendaRepository.findAgendadasNoPeriodo(usuario.getTenantId(), usuario.getId(), inicio, fim))
                .thenReturn(List.of(venda));
        when(clienteContatoRepository.findAllByClienteOrganizationIdAndCreatedBy(usuario.getTenantId(), usuario.getId()))
                .thenReturn(List.of(contato));

        Page<EventoAgenda> pagina = agendaService.listarEventos(usuario, pesquisa);

        assertThat(pagina.getTotalElements()).isEqualTo(5);
        assertThat(pagina.getContent())
                .extracting(EventoAgenda::getTipo)
                .contains(TipoEvento.PAGAMENTO, TipoEvento.AGENDAMENTO, TipoEvento.ANIVERSARIO);

        assertThat(pagina.getContent())
                .filteredOn(evento -> evento.getTipo() == TipoEvento.AGENDAMENTO && evento.getReferencia() == TipoEvento.COMPRA)
                .isNotEmpty();
        assertThat(pagina.getContent())
                .filteredOn(evento -> evento.getTipo() == TipoEvento.AGENDAMENTO && evento.getReferencia() == TipoEvento.VENDA)
                .isNotEmpty();
        assertThat(pagina.getContent())
                .filteredOn(evento -> evento.getTipo() == TipoEvento.ANIVERSARIO)
                .first()
                .extracting(EventoAgenda::getData)
                .isEqualTo(LocalDate.of(2024, 1, 15));

        assertThat(pagina.getContent())
                .allMatch(evento -> evento.getTitulo() != null && !evento.getTitulo().isBlank());
    }

    @Test
    void listarEventosRespeitaJanelaDeDatas() {
        LocalDate inicio = LocalDate.of(2024, 6, 1);
        LocalDate fim = inicio.plusDays(3);

        AgendaSearch pesquisa = AgendaSearch.builder()
                .page(0)
                .pageSize(10)
                .dataInicial(inicio)
                .dataFinal(fim)
                .build();

        CompraPagamento pagamentoDentro = CompraPagamento.builder()
                .dataVencimento(inicio.plusDays(1))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();
        pagamentoDentro.setId(10);

        CompraPagamento pagamentoFora = CompraPagamento.builder()
                .dataVencimento(inicio.plusDays(10))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();
        pagamentoFora.setId(11);

        Compra compraDentro = Compra.builder()
                .dataAgendada(inicio.plusDays(2))
                .sequencialUsuario(1)
                .build();
        compraDentro.setId(1);

        Compra compraFora = Compra.builder()
                .dataAgendada(inicio.plusDays(7))
                .sequencialUsuario(2)
                .build();
        compraFora.setId(2);

        ClienteContato contatoFora = ClienteContato.builder()
                .nome("Contato Externo")
                .aniversario("10/06")
                .build();
        contatoFora.setId(5);

        when(pagamentoCompraRepository.findAllByCompraOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(
                eq(usuario.getTenantId()), eq(StatusPagamento.PENDENTE), eq(inicio)))
                .thenReturn(List.of(pagamentoDentro, pagamentoFora));
        when(pagamentoVendaRepository.findAllByVendaOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(
                eq(usuario.getTenantId()), eq(StatusPagamento.PENDENTE), eq(inicio)))
                .thenReturn(List.of());
        when(compraRepository.findAgendadasNoPeriodo(usuario.getTenantId(), usuario.getId(), inicio, fim))
                .thenReturn(List.of(compraDentro, compraFora));
        when(vendaRepository.findAgendadasNoPeriodo(usuario.getTenantId(), usuario.getId(), inicio, fim))
                .thenReturn(List.of());
        when(clienteContatoRepository.findAllByClienteOrganizationIdAndCreatedBy(usuario.getTenantId(), usuario.getId()))
                .thenReturn(List.of(contatoFora));

        Page<EventoAgenda> pagina = agendaService.listarEventos(usuario, pesquisa);

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent())
                .allMatch(evento -> !evento.getData().isBefore(inicio) && !evento.getData().isAfter(fim));
        assertThat(pagina.getContent())
                .extracting(EventoAgenda::getTipo)
                .doesNotContain(TipoEvento.ANIVERSARIO);
    }
}
