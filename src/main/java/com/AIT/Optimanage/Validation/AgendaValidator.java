package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraServico;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgendaValidator {

    private static final int DEFAULT_MAX_WINDOW_DAYS = 365;
    private static final Duration DEFAULT_DURATION = Duration.ofHours(1);

    private final CompraRepository compraRepository;
    private final VendaRepository vendaRepository;
    private final Clock clock;

    public LocalDate validarDataAgendamento(String dataAgendada) {
        return validarDataAgendamento(dataAgendada, DEFAULT_MAX_WINDOW_DAYS);
    }

    public LocalDate validarDataAgendamento(String dataAgendada, Integer janelaMaximaDias) {
        if (dataAgendada == null || dataAgendada.isBlank()) {
            throw new IllegalArgumentException("Informe a data do agendamento no formato yyyy-MM-dd.");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(dataAgendada, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data agendada deve estar no formato ISO (yyyy-MM-dd).");
        }

        LocalDate hoje = LocalDate.now(clock);
        if (data.isBefore(hoje)) {
            throw new IllegalArgumentException("Não é possível agendar para uma data no passado.");
        }

        if (janelaMaximaDias != null && janelaMaximaDias > 0) {
            LocalDate limite = hoje.plusDays(janelaMaximaDias);
            if (data.isAfter(limite)) {
                throw new IllegalArgumentException(
                        "A data informada ultrapassa o limite máximo de " + janelaMaximaDias + " dias.");
            }
        }
        return data;
    }

    public LocalTime validarHoraAgendada(String horaAgendada) {
        if (horaAgendada == null || horaAgendada.isBlank()) {
            throw new IllegalArgumentException("Informe o horário do agendamento no formato HH:mm.");
        }
        try {
            return LocalTime.parse(horaAgendada, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Horário agendado deve estar no formato HH:mm.");
        }
    }

    public Duration validarDuracao(Integer duracaoEmMinutos) {
        if (duracaoEmMinutos == null) {
            return DEFAULT_DURATION;
        }
        if (duracaoEmMinutos <= 0) {
            throw new IllegalArgumentException("A duração deve ser informada em minutos e maior que zero.");
        }
        return Duration.ofMinutes(duracaoEmMinutos.longValue());
    }

    public void validarConflitosAgendamentoCompra(Compra compra, Integer userId, LocalDate data, LocalTime hora,
                                                   Duration duracao) {
        if (compra == null || data == null) {
            return;
        }

        Integer organizationId = compra.getOrganizationId();
        Agendamentos agendamentos = carregarAgendamentos(organizationId, userId, data);
        verificarConflitosUsuario(agendamentos, data, hora, duracao, compra.getId(), null);

        Set<Integer> recursos = extrairRecursosCompra(compra.getCompraServicos());
        if (recursos.isEmpty()) {
            return;
        }

        for (Compra outraCompra : agendamentos.compras()) {
            if (Objects.equals(outraCompra.getId(), compra.getId())) {
                continue;
            }
            if (recursosConflitantes(recursos, extrairRecursosCompra(outraCompra.getCompraServicos()))
                    && intervaloConflitante(data, hora, duracao, outraCompra.getDataAgendada(),
                    outraCompra.getHoraAgendada(), outraCompra.getDuracaoEstimada())) {
                throw new IllegalArgumentException("Existe conflito de recursos (serviços) no período selecionado.");
            }
        }
    }

    public void validarConflitosAgendamentoVenda(User usuario, Venda venda, LocalDate data, LocalTime hora,
                                                 Duration duracao) {
        if (venda == null || data == null) {
            return;
        }

        Integer organizationId = venda.getOrganizationId();
        Integer userId = Optional.ofNullable(usuario).map(User::getId).orElse(venda.getCreatedBy());
        Agendamentos agendamentos = carregarAgendamentos(organizationId, userId, data);
        verificarConflitosUsuario(agendamentos, data, hora, duracao, null, venda.getId());

        Integer clienteId = Optional.ofNullable(venda.getCliente()).map(cliente -> cliente.getId()).orElse(null);
        if (clienteId != null) {
            for (Venda outraVenda : agendamentos.vendas()) {
                if (Objects.equals(outraVenda.getId(), venda.getId())) {
                    continue;
                }
                Integer outroClienteId = Optional.ofNullable(outraVenda.getCliente())
                        .map(cliente -> cliente.getId()).orElse(null);
                if (Objects.equals(clienteId, outroClienteId)
                        && intervaloConflitante(data, hora, duracao, outraVenda.getDataAgendada(),
                        outraVenda.getHoraAgendada(), outraVenda.getDuracaoEstimada())) {
                    throw new IllegalArgumentException("O cliente selecionado já possui um agendamento neste período.");
                }
            }
        }

        Set<Integer> recursos = extrairRecursosVenda(venda.getVendaServicos());
        if (recursos.isEmpty()) {
            return;
        }

        for (Venda outraVenda : agendamentos.vendas()) {
            if (Objects.equals(outraVenda.getId(), venda.getId())) {
                continue;
            }
            if (recursosConflitantes(recursos, extrairRecursosVenda(outraVenda.getVendaServicos()))
                    && intervaloConflitante(data, hora, duracao, outraVenda.getDataAgendada(),
                    outraVenda.getHoraAgendada(), outraVenda.getDuracaoEstimada())) {
                throw new IllegalArgumentException("Existe conflito de recursos (serviços) no período selecionado.");
            }
        }
    }

    private Agendamentos carregarAgendamentos(Integer organizationId, Integer userId, LocalDate data) {
        if (organizationId == null || userId == null || data == null) {
            return new Agendamentos(List.of(), List.of());
        }
        List<Compra> compras = compraRepository.findAgendadasNoPeriodo(organizationId, userId, data, data);
        List<Venda> vendas = vendaRepository.findAgendadasNoPeriodo(organizationId, userId, data, data);
        return new Agendamentos(compras, vendas);
    }

    private void verificarConflitosUsuario(Agendamentos agendamentos, LocalDate data, LocalTime hora, Duration duracao,
                                           Integer ignorarCompraId, Integer ignorarVendaId) {
        for (Compra outraCompra : agendamentos.compras()) {
            if (Objects.equals(outraCompra.getId(), ignorarCompraId)) {
                continue;
            }
            if (intervaloConflitante(data, hora, duracao, outraCompra.getDataAgendada(),
                    outraCompra.getHoraAgendada(), outraCompra.getDuracaoEstimada())) {
                throw new IllegalArgumentException("Já existe um agendamento para este usuário no período informado.");
            }
        }

        for (Venda outraVenda : agendamentos.vendas()) {
            if (Objects.equals(outraVenda.getId(), ignorarVendaId)) {
                continue;
            }
            if (intervaloConflitante(data, hora, duracao, outraVenda.getDataAgendada(),
                    outraVenda.getHoraAgendada(), outraVenda.getDuracaoEstimada())) {
                throw new IllegalArgumentException("Já existe um agendamento para este usuário no período informado.");
            }
        }
    }

    private boolean intervaloConflitante(LocalDate dataReferencia, LocalTime horaReferencia, Duration duracaoReferencia,
                                         LocalDate outraData, LocalTime outraHora, Duration outraDuracao) {
        if (dataReferencia == null || outraData == null || !dataReferencia.equals(outraData)) {
            return false;
        }

        DateTimeRange atual = construirIntervalo(dataReferencia, horaReferencia, duracaoReferencia);
        DateTimeRange existente = construirIntervalo(outraData, outraHora, outraDuracao);
        return atual.inicio().isBefore(existente.fim()) && existente.inicio().isBefore(atual.fim());
    }

    private DateTimeRange construirIntervalo(LocalDate data, LocalTime hora, Duration duracao) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);

        if (hora != null) {
            inicio = data.atTime(hora);
            Duration efetiva = (duracao == null || duracao.isZero() || duracao.isNegative())
                    ? DEFAULT_DURATION : duracao;
            LocalDateTime calculado = inicio.plus(efetiva);
            if (calculado.toLocalDate().isAfter(data)) {
                fim = data.atTime(LocalTime.MAX);
            } else {
                fim = calculado;
            }
        }

        return new DateTimeRange(inicio, fim);
    }

    private Set<Integer> extrairRecursosCompra(Collection<CompraServico> servicos) {
        return Optional.ofNullable(servicos).orElseGet(List::of).stream()
                .map(servico -> Optional.ofNullable(servico.getServico())
                        .map(s -> s.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Integer> extrairRecursosVenda(Collection<VendaServico> servicos) {
        return Optional.ofNullable(servicos).orElseGet(List::of).stream()
                .map(servico -> Optional.ofNullable(servico.getServico())
                        .map(s -> s.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean recursosConflitantes(Set<Integer> recursosAtuais, Set<Integer> recursosExistentes) {
        if (recursosAtuais.isEmpty() || recursosExistentes.isEmpty()) {
            return false;
        }
        return recursosAtuais.stream().anyMatch(recursosExistentes::contains);
    }

    private record Agendamentos(List<Compra> compras, List<Venda> vendas) {
    }

    private record DateTimeRange(LocalDateTime inicio, LocalDateTime fim) {
    }
}
