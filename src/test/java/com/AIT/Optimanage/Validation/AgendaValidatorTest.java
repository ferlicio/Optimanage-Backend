package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraServico;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaValidatorTest {

    @Mock
    private CompraRepository compraRepository;
    @Mock
    private VendaRepository vendaRepository;

    private AgendaValidator agendaValidator;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(LocalDate.of(2024, 1, 10).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        agendaValidator = new AgendaValidator(compraRepository, vendaRepository, clock);
    }

    @Test
    void validarDataAgendamentoRejeitaDatasPassadas() {
        assertThatThrownBy(() -> agendaValidator.validarDataAgendamento("2024-01-05"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("passado");
    }

    @Test
    void validarConflitoPorClienteEmVenda() {
        LocalDate data = LocalDate.of(2024, 1, 15);
        LocalTime hora = LocalTime.of(10, 0);
        Duration duracao = Duration.ofMinutes(90);

        Cliente cliente = new Cliente();
        cliente.setId(20);

        Venda venda = Venda.builder().build();
        venda.setId(1);
        venda.setOrganizationId(5);
        venda.setCreatedBy(8);
        venda.setCliente(cliente);

        Venda outraVenda = Venda.builder().build();
        outraVenda.setId(2);
        outraVenda.setOrganizationId(5);
        outraVenda.setCreatedBy(8);
        outraVenda.setCliente(cliente);
        outraVenda.setDataAgendada(data);
        outraVenda.setHoraAgendada(hora);
        outraVenda.setDuracaoEstimada(Duration.ofMinutes(60));

        when(compraRepository.findAgendadasNoPeriodo(eq(5), eq(8), eq(data), eq(data))).thenReturn(List.of());
        when(vendaRepository.findAgendadasNoPeriodo(eq(5), eq(8), eq(data), eq(data)))
                .thenReturn(List.of(outraVenda));

        User usuario = User.builder().id(8).tenantId(5).build();

        assertThatThrownBy(() -> agendaValidator.validarConflitosAgendamentoVenda(usuario, venda, data, hora, duracao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cliente");
    }

    @Test
    void validarAgendamentoSemConflitosTemSucesso() {
        LocalDate data = LocalDate.of(2024, 1, 18);
        LocalTime hora = LocalTime.of(9, 0);
        Duration duracao = Duration.ofMinutes(60);

        Servico servico = Servico.builder().id(33).build();
        CompraServico compraServico = CompraServico.builder().servico(servico).build();
        Compra compra = Compra.builder().build();
        compra.setId(3);
        compra.setOrganizationId(5);
        compra.setCreatedBy(8);
        compra.setCompraServicos(List.of(compraServico));

        VendaServico vendaServico = VendaServico.builder().servico(servico).build();
        Venda vendaExistente = Venda.builder().build();
        vendaExistente.setId(4);
        vendaExistente.setOrganizationId(5);
        vendaExistente.setCreatedBy(8);
        vendaExistente.setDataAgendada(data);
        vendaExistente.setHoraAgendada(LocalTime.of(13, 0));
        vendaExistente.setDuracaoEstimada(Duration.ofMinutes(60));
        vendaExistente.setVendaServicos(List.of(vendaServico));

        when(compraRepository.findAgendadasNoPeriodo(eq(5), eq(8), eq(data), eq(data)))
                .thenReturn(List.of(compra));
        when(vendaRepository.findAgendadasNoPeriodo(eq(5), eq(8), eq(data), eq(data)))
                .thenReturn(List.of(vendaExistente));

        Compra novaCompra = Compra.builder().build();
        novaCompra.setId(9);
        novaCompra.setOrganizationId(5);
        novaCompra.setCreatedBy(8);
        novaCompra.setCompraServicos(List.of(compraServico));

        assertThatCode(() -> agendaValidator.validarConflitosAgendamentoCompra(novaCompra, 8, data, hora, duracao))
                .doesNotThrowAnyException();
    }
}
