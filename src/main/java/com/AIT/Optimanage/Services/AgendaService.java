package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Agenda.AgendaSearch;
import com.AIT.Optimanage.Models.Agenda.EventoAgenda;
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
import com.AIT.Optimanage.Services.PlanoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class AgendaService {
    private final PagamentoCompraRepository pagamentoCompraRepository;
    private final PagamentoVendaRepository pagamentoVendaRepository;
    private final CompraRepository compraRepository;
    private final VendaRepository vendaRepository;
    private final ClienteContatoRepository clienteContatoRepository;
    private final PlanoService planoService;

    public Page<EventoAgenda> listarEventos(User loggedUser, AgendaSearch pesquisa) {
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Plano plano = planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        if (!Boolean.TRUE.equals(plano.getAgendaHabilitada())) {
            throw new AccessDeniedException("Agenda não está habilitada no plano atual");
        }

        AgendaSearch filtros = ofNullable(pesquisa).orElseGet(AgendaSearch::new);

        Sort.Direction direction = ofNullable(filtros.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = ofNullable(filtros.getSort()).orElse("data");
        int pageNumber = ofNullable(filtros.getPage()).orElse(0);
        int pageSize = ofNullable(filtros.getPageSize()).orElse(20);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        LocalDate now = LocalDate.now();
        LocalDate inicio = ofNullable(filtros.getDataInicial()).orElse(now);
        LocalDate fim = filtros.getDataFinal();

        List<EventoAgenda> eventos = new ArrayList<>();

        Integer organizationId = loggedUser.getTenantId();
        Integer userId = loggedUser.getId();

        List<CompraPagamento> compras = pagamentoCompraRepository
                .findAllByCompraOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(organizationId, StatusPagamento.PENDENTE, inicio);
        compras.stream()
                .filter(p -> withinRange(p.getDataVencimento(), inicio, fim))
                .map(pagamento -> criarEvento(TipoEvento.PAGAMENTO, TipoEvento.COMPRA, pagamento.getDataVencimento(), null, null,
                        pagamento.getId(), montarTituloPagamentoCompra(pagamento), montarDescricaoPagamentoCompra(pagamento)))
                .forEach(eventos::add);

        List<VendaPagamento> vendas = pagamentoVendaRepository
                .findAllByVendaOrganizationIdAndStatusPagamentoAndDataVencimentoGreaterThanEqual(organizationId, StatusPagamento.PENDENTE, inicio);
        vendas.stream()
                .filter(p -> withinRange(p.getDataVencimento(), inicio, fim))
                .map(pagamento -> criarEvento(TipoEvento.PAGAMENTO, TipoEvento.VENDA, pagamento.getDataVencimento(), null, null,
                        pagamento.getId(), montarTituloPagamentoVenda(pagamento), montarDescricaoPagamentoVenda(pagamento)))
                .forEach(eventos::add);

        compraRepository.findAgendadasNoPeriodo(organizationId, userId, inicio, fim).stream()
                .filter(compra -> withinRange(compra.getDataAgendada(), inicio, fim))
                .map(compra -> criarEvento(TipoEvento.AGENDAMENTO, TipoEvento.COMPRA, compra.getDataAgendada(),
                        compra.getHoraAgendada(), compra.getDuracaoEstimada(), compra.getId(), montarTituloCompra(compra),
                        montarDescricaoCompra(compra)))
                .forEach(eventos::add);

        vendaRepository.findAgendadasNoPeriodo(organizationId, userId, inicio, fim).stream()
                .filter(venda -> withinRange(venda.getDataAgendada(), inicio, fim))
                .map(venda -> criarEvento(TipoEvento.AGENDAMENTO, TipoEvento.VENDA, venda.getDataAgendada(), venda.getHoraAgendada(),
                        venda.getDuracaoEstimada(), venda.getId(), montarTituloVenda(venda), montarDescricaoVenda(venda)))
                .forEach(eventos::add);

        clienteContatoRepository.findAllByClienteOrganizationIdAndCreatedBy(organizationId, userId).stream()
                .map(contato -> normalizarAniversario(contato.getAniversario(), inicio, fim)
                        .map(data -> criarEvento(TipoEvento.ANIVERSARIO, TipoEvento.ANIVERSARIO, data, null, null, contato.getId(),
                                contato.getNome(), "Aniversário de " + contato.getNome())))
                .flatMap(Optional::stream)
                .forEach(eventos::add);

        Map<String, EventoAgenda> eventosUnicos = new LinkedHashMap<>();
        for (EventoAgenda evento : eventos) {
            if (evento.getData() == null) {
                continue;
            }
            String chave = gerarChaveEvento(evento);
            EventoAgenda existente = eventosUnicos.get(chave);
            if (existente == null || evento.getData().isBefore(existente.getData())) {
                eventosUnicos.put(chave, evento);
            }
        }

        Comparator<EventoAgenda> comparator = Comparator.comparing(EventoAgenda::getData)
                .thenComparing(evento -> ofNullable(evento.getHora()).orElse(LocalTime.MIN))
                .thenComparing(evento -> ofNullable(evento.getTipo()).map(Enum::name).orElse(""))
                .thenComparing(evento -> ofNullable(evento.getReferencia()).map(Enum::name).orElse(""))
                .thenComparing(evento -> ofNullable(evento.getId()).orElse(0));

        List<EventoAgenda> ordenados = new ArrayList<>(eventosUnicos.values());
        ordenados.sort(direction.isAscending() ? comparator : comparator.reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), ordenados.size());
        List<EventoAgenda> content = start >= ordenados.size() ? List.of() : ordenados.subList(start, end);

        return new PageImpl<>(content, pageable, ordenados.size());
    }

    private EventoAgenda criarEvento(TipoEvento tipo, TipoEvento referencia, LocalDate data, LocalTime hora, Duration duracao,
            Integer id, String titulo, String descricao) {
        return EventoAgenda.builder()
                .tipo(tipo)
                .referencia(referencia)
                .data(data)
                .hora(hora)
                .duracao(duracao)
                .id(id)
                .titulo(titulo)
                .descricao(descricao)
                .build();
    }

    private boolean withinRange(LocalDate data, LocalDate inicio, LocalDate fim) {
        if (data == null) {
            return false;
        }
        if (data.isBefore(inicio)) {
            return false;
        }
        return fim == null || !data.isAfter(fim);
    }

    private Optional<LocalDate> normalizarAniversario(String aniversario, LocalDate inicio, LocalDate fim) {
        if (aniversario == null || aniversario.isBlank()) {
            return Optional.empty();
        }

        MonthDay mesDia = parseMonthDay(aniversario.trim());
        if (mesDia == null) {
            return Optional.empty();
        }

        LocalDate data = ajustarParaAno(mesDia, inicio.getYear());
        while (data != null && data.isBefore(inicio)) {
            data = ajustarParaAno(mesDia, data.getYear() + 1);
        }

        if (data == null || (fim != null && data.isAfter(fim))) {
            return Optional.empty();
        }

        return Optional.of(data);
    }

    private MonthDay parseMonthDay(String aniversario) {
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try {
                LocalDate parsed = LocalDate.parse(aniversario, formatter);
                return MonthDay.of(parsed.getMonth(), parsed.getDayOfMonth());
            } catch (DateTimeParseException ignored) {
                // tenta próximo formato
            }
        }
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ofPattern("dd/MM"), DateTimeFormatter.ofPattern("dd-MM"))) {
            try {
                return MonthDay.parse(aniversario, formatter);
            } catch (DateTimeParseException ignored) {
                // tenta próximo formato
            }
        }
        return null;
    }

    private LocalDate ajustarParaAno(MonthDay mesDia, int ano) {
        try {
            return mesDia.atYear(ano);
        } catch (RuntimeException ex) {
            if (mesDia.equals(MonthDay.of(2, 29))) {
                return LocalDate.of(ano, 2, 28);
            }
            return null;
        }
    }

    private String gerarChaveEvento(EventoAgenda evento) {
        return String.join(":",
                ofNullable(evento.getTipo()).map(Enum::name).orElse(""),
                ofNullable(evento.getReferencia()).map(Enum::name).orElse(""),
                ofNullable(evento.getId()).map(String::valueOf).orElse(""));
    }

    private String montarTituloPagamentoCompra(CompraPagamento pagamento) {
        String numeroCompra = ofNullable(pagamento.getCompra())
                .map(Compra::getSequencialUsuario)
                .map(seq -> "#" + seq)
                .orElse("");
        return ("Pagamento de compra " + numeroCompra).trim();
    }

    private String montarDescricaoPagamentoCompra(CompraPagamento pagamento) {
        return "Parcela de compra vence em " + pagamento.getDataVencimento();
    }

    private String montarTituloPagamentoVenda(VendaPagamento pagamento) {
        String numeroVenda = ofNullable(pagamento.getVenda())
                .map(Venda::getSequencialUsuario)
                .map(seq -> "#" + seq)
                .orElse("");
        return ("Pagamento de venda " + numeroVenda).trim();
    }

    private String montarDescricaoPagamentoVenda(VendaPagamento pagamento) {
        return "Parcela de venda vence em " + pagamento.getDataVencimento();
    }

    private String montarTituloCompra(Compra compra) {
        return "Compra agendada #" + ofNullable(compra.getSequencialUsuario()).orElse(0);
    }

    private String montarDescricaoCompra(Compra compra) {
        String fornecedor = ofNullable(compra.getFornecedor())
                .map(f -> ofNullable(f.getNome()).orElse(null))
                .filter(Objects::nonNull)
                .orElse(null);
        if (fornecedor != null && !fornecedor.isBlank()) {
            return "Visita ao fornecedor " + fornecedor;
        }
        return ofNullable(compra.getObservacoes()).filter(obs -> !obs.isBlank()).orElse("Compra agendada");
    }

    private String montarTituloVenda(Venda venda) {
        return "Venda agendada #" + ofNullable(venda.getSequencialUsuario()).orElse(0);
    }

    private String montarDescricaoVenda(Venda venda) {
        String cliente = ofNullable(venda.getCliente())
                .map(c -> ofNullable(c.getNome()).orElse(null))
                .filter(Objects::nonNull)
                .orElse(null);
        if (cliente != null && !cliente.isBlank()) {
            return "Contato com cliente " + cliente;
        }
        return ofNullable(venda.getObservacoes()).filter(obs -> !obs.isBlank()).orElse("Venda agendada");
    }
}
