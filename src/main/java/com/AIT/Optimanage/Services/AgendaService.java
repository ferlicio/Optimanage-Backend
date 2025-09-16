package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Agenda.AgendaSearch;
import com.AIT.Optimanage.Models.Agenda.EventoAgenda;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Enums.TipoEvento;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.Compra.PagamentoCompraRepository;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Services.PlanoService;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgendaService {
    private final PagamentoCompraRepository pagamentoCompraRepository;
    private final PagamentoVendaRepository pagamentoVendaRepository;
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
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("data");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        LocalDate now = LocalDate.now();
        LocalDate inicio = Optional.ofNullable(pesquisa.getDataInicial()).orElse(now);
        LocalDate fim = Optional.ofNullable(pesquisa.getDataFinal()).orElse(LocalDate.MAX);

        List<EventoAgenda> eventos = new ArrayList<>();

        List<CompraPagamento> compras = pagamentoCompraRepository
                .findAllByCompraOwnerUserAndStatusPagamentoAndDataVencimentoAfter(loggedUser, StatusPagamento.PENDENTE, now);
        compras.stream()
                .filter(p -> !p.getDataVencimento().isBefore(inicio) && !p.getDataVencimento().isAfter(fim))
                .forEach(p -> eventos.add(EventoAgenda.builder()
                        .tipo(TipoEvento.PAGAMENTO)
                        .data(p.getDataVencimento())
                        .id(p.getId())
                        .build()));

        List<VendaPagamento> vendas = pagamentoVendaRepository
                .findAllByVendaOwnerUserAndStatusPagamentoAndDataVencimentoAfter(loggedUser, StatusPagamento.PENDENTE, now);
        vendas.stream()
                .filter(p -> !p.getDataVencimento().isBefore(inicio) && !p.getDataVencimento().isAfter(fim))
                .forEach(p -> eventos.add(EventoAgenda.builder()
                        .tipo(TipoEvento.PAGAMENTO)
                        .data(p.getDataVencimento())
                        .id(p.getId())
                        .build()));

        eventos.sort(direction.isAscending()
                ? Comparator.comparing(EventoAgenda::getData)
                : Comparator.comparing(EventoAgenda::getData).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), eventos.size());
        List<EventoAgenda> content = start >= eventos.size() ? List.of() : eventos.subList(start, end);

        return new PageImpl<>(content, pageable, eventos.size());
    }
}
