package com.AIT.Optimanage.Services.CashFlow;

import com.AIT.Optimanage.Mappers.CashFlowMapper;
import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryRequest;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryResponse;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowOrigin;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import com.AIT.Optimanage.Models.CashFlow.Search.CashFlowSearch;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowEntryRepository;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowFilters;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.FilterBuilder;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowEntryRepository repository;
    private final VendaRepository vendaRepository;
    private final CompraRepository compraRepository;
    private final CashFlowMapper mapper;

    @Transactional(readOnly = true)
    public Page<CashFlowEntryResponse> listarLancamentos(CashFlowSearch search) {
        Integer organizationId = getOrganizationId();

        Sort.Direction direction = Optional.ofNullable(search.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);
        String sortBy = Optional.ofNullable(search.getSort()).orElse("movementDate");

        int page = Optional.ofNullable(search.getPage()).orElse(0);
        int pageSize = Optional.ofNullable(search.getPageSize()).filter(size -> size > 0).orElse(20);
        int safePage = Math.max(page, 0);
        long fetchSizeLong = (long) (safePage + 1) * pageSize;
        int fetchSize = fetchSizeLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fetchSizeLong;

        Specification<CashFlowEntry> spec = FilterBuilder
                .of(CashFlowFilters.hasOrganization(organizationId))
                .and(search.getType(), CashFlowFilters::hasType)
                .and(search.getStatus(), CashFlowFilters::hasStatus)
                .and(search.getStartDate(), CashFlowFilters::occursOnOrAfter)
                .and(search.getEndDate(), CashFlowFilters::occursOnOrBefore)
                .build();

        Comparator<CashFlowEntryResponse> comparator = buildComparator(sortBy, direction);

        Pageable manualPageable = PageRequest.of(0, fetchSize, Sort.by(direction, mapManualSortProperty(sortBy)));
        Page<CashFlowEntry> manualPage = repository.findAll(spec, manualPageable);
        var manualEntries = manualPage.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toCollection(ArrayList::new));

        var sales = listarVendas(organizationId, search, fetchSize, direction, sortBy);
        var purchases = listarCompras(organizationId, search, fetchSize, direction, sortBy);

        long totalElements = manualPage.getTotalElements() + sales.getTotalElements() + purchases.getTotalElements();

        var allEntries = Stream.of(manualEntries, sales.getEntries(), purchases.getEntries())
                .flatMap(ArrayList::stream)
                .sorted(comparator)
                .collect(Collectors.toList());

        int fromIndex = Math.min(safePage * pageSize, allEntries.size());
        int toIndex = Math.min(fromIndex + pageSize, allEntries.size());
        Pageable pageable = PageRequest.of(safePage, pageSize, Sort.by(direction, sortBy));
        return new PageImpl<>(allEntries.subList(fromIndex, toIndex), pageable, totalElements);
    }

    @Transactional(readOnly = true)
    public CashFlowEntryResponse buscarLancamento(Integer id) {
        return mapper.toResponse(getEntry(id));
    }

    @Transactional
    public CashFlowEntryResponse criarLancamento(CashFlowEntryRequest request) {
        Integer organizationId = getOrganizationId();
        CashFlowEntry entry = mapper.toEntity(request);
        entry.setStatus(resolveStatus(entry.getMovementDate()));
        entry.setCancelledAt(null);
        entry.setOrganizationId(organizationId);
        CashFlowEntry saved = repository.save(entry);
        return mapper.toResponse(saved);
    }

    @Transactional
    public CashFlowEntryResponse atualizarLancamento(Integer id, CashFlowEntryRequest request) {
        CashFlowEntry entry = getEntry(id);
        if (entry.getStatus() == CashFlowStatus.CANCELLED) {
            throw new IllegalStateException("Não é possível editar um lançamento cancelado.");
        }
        mapper.updateEntityFromRequest(request, entry);
        entry.setStatus(resolveStatus(entry.getMovementDate()));
        entry.setCancelledAt(null);
        CashFlowEntry saved = repository.save(entry);
        return mapper.toResponse(saved);
    }

    @Transactional
    public CashFlowEntryResponse cancelarLancamento(Integer id) {
        CashFlowEntry entry = getEntry(id);
        if (entry.getStatus() == CashFlowStatus.CANCELLED) {
            return mapper.toResponse(entry);
        }
        entry.setStatus(CashFlowStatus.CANCELLED);
        entry.setCancelledAt(LocalDateTime.now());
        CashFlowEntry saved = repository.save(entry);
        return mapper.toResponse(saved);
    }

    private CashFlowEntry getEntry(Integer id) {
        Integer organizationId = getOrganizationId();
        return repository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento de fluxo de caixa não encontrado"));
    }

    private CashFlowStatus resolveStatus(LocalDate movementDate) {
        if (movementDate != null && movementDate.isAfter(LocalDate.now())) {
            return CashFlowStatus.SCHEDULED;
        }
        return CashFlowStatus.ACTIVE;
    }

    private Comparator<CashFlowEntryResponse> buildComparator(String sortBy, Sort.Direction direction) {
        Comparator<CashFlowEntryResponse> comparator;
        switch (Optional.ofNullable(sortBy).orElse("movementDate")) {
            case "amount" -> comparator = Comparator.comparing(CashFlowEntryResponse::getAmount,
                    Comparator.nullsLast(java.math.BigDecimal::compareTo));
            case "description" -> comparator = Comparator.comparing(CashFlowEntryResponse::getDescription,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "createdAt" -> comparator = Comparator.comparing(CashFlowEntryResponse::getCreatedAt,
                    Comparator.nullsLast(LocalDateTime::compareTo));
            case "movementDate" -> comparator = Comparator.comparing(CashFlowEntryResponse::getMovementDate,
                    Comparator.nullsLast(LocalDate::compareTo));
            default -> comparator = Comparator.comparing(CashFlowEntryResponse::getMovementDate,
                    Comparator.nullsLast(LocalDate::compareTo));
        }
        if (direction == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private SourcePage listarVendas(Integer organizationId, CashFlowSearch search, int fetchSize,
                                    Sort.Direction direction, String sortBy) {
        if (search.getType() != null && search.getType() != CashFlowType.INCOME) {
            return SourcePage.empty();
        }

        Specification<Venda> spec = buildVendaSpecification(organizationId, search);
        Pageable pageable = PageRequest.of(0, fetchSize, buildVendaSort(sortBy, direction));
        Page<Venda> page = vendaRepository.findAll(spec, pageable);
        ArrayList<CashFlowEntryResponse> entries = page.getContent().stream()
                .map(this::toCashFlowSale)
                .collect(Collectors.toCollection(ArrayList::new));
        return new SourcePage(entries, page.getTotalElements());
    }

    private SourcePage listarCompras(Integer organizationId, CashFlowSearch search, int fetchSize,
                                     Sort.Direction direction, String sortBy) {
        if (search.getType() != null && search.getType() != CashFlowType.EXPENSE) {
            return SourcePage.empty();
        }

        Specification<Compra> spec = buildCompraSpecification(organizationId, search);
        Pageable pageable = PageRequest.of(0, fetchSize, buildCompraSort(sortBy, direction));
        Page<Compra> page = compraRepository.findAll(spec, pageable);
        ArrayList<CashFlowEntryResponse> entries = page.getContent().stream()
                .map(this::toCashFlowPurchase)
                .collect(Collectors.toCollection(ArrayList::new));
        return new SourcePage(entries, page.getTotalElements());
    }

    private Specification<Venda> buildVendaSpecification(Integer organizationId, CashFlowSearch search) {
        Specification<Venda> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("organizationId"), organizationId));

        if (search.getStartDate() != null) {
            spec = spec.and(vendaOccursOnOrAfter(search.getStartDate()));
        }
        if (search.getEndDate() != null) {
            spec = spec.and(vendaOccursOnOrBefore(search.getEndDate()));
        }
        if (search.getStatus() != null) {
            spec = spec.and(vendaHasCashFlowStatus(search.getStatus()));
        }
        return spec;
    }

    private Specification<Compra> buildCompraSpecification(Integer organizationId, CashFlowSearch search) {
        Specification<Compra> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("organizationId"), organizationId));

        if (search.getStartDate() != null) {
            spec = spec.and(compraOccursOnOrAfter(search.getStartDate()));
        }
        if (search.getEndDate() != null) {
            spec = spec.and(compraOccursOnOrBefore(search.getEndDate()));
        }
        if (search.getStatus() != null) {
            spec = spec.and(compraHasCashFlowStatus(search.getStatus()));
        }
        return spec;
    }

    private Specification<Venda> vendaOccursOnOrAfter(LocalDate date) {
        return (root, query, cb) -> {
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            return cb.greaterThanOrEqualTo(movementDate, date);
        };
    }

    private Specification<Venda> vendaOccursOnOrBefore(LocalDate date) {
        return (root, query, cb) -> {
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            return cb.lessThanOrEqualTo(movementDate, date);
        };
    }

    private Specification<Venda> vendaHasCashFlowStatus(CashFlowStatus status) {
        return (root, query, cb) -> {
            var statusPath = root.get("status");
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            LocalDate today = LocalDate.now();

            return switch (status) {
                case CANCELLED -> cb.equal(statusPath, StatusVenda.CANCELADA);
                case SCHEDULED -> {
                    var scheduledStatuses = statusPath.in(StatusVenda.ORCAMENTO, StatusVenda.PENDENTE, StatusVenda.AGENDADA);
                    var notCancelled = cb.notEqual(statusPath, StatusVenda.CANCELADA);
                    var movementAfterToday = cb.greaterThan(movementDate, today);
                    yield cb.or(scheduledStatuses, cb.and(notCancelled, cb.not(scheduledStatuses), movementAfterToday));
                }
                case ACTIVE -> {
                    var notCancelled = cb.notEqual(statusPath, StatusVenda.CANCELADA);
                    var scheduledStatuses = statusPath.in(StatusVenda.ORCAMENTO, StatusVenda.PENDENTE, StatusVenda.AGENDADA);
                    yield cb.and(notCancelled, cb.not(scheduledStatuses), cb.lessThanOrEqualTo(movementDate, today));
                }
            };
        };
    }

    private Specification<Compra> compraOccursOnOrAfter(LocalDate date) {
        return (root, query, cb) -> {
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            return cb.greaterThanOrEqualTo(movementDate, date);
        };
    }

    private Specification<Compra> compraOccursOnOrBefore(LocalDate date) {
        return (root, query, cb) -> {
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            return cb.lessThanOrEqualTo(movementDate, date);
        };
    }

    private Specification<Compra> compraHasCashFlowStatus(CashFlowStatus status) {
        return (root, query, cb) -> {
            var statusPath = root.get("status");
            var movementDate = cb.coalesce(root.<LocalDate>get("dataEfetuacao"), root.<LocalDate>get("dataAgendada"));
            LocalDate today = LocalDate.now();

            return switch (status) {
                case CANCELLED -> cb.equal(statusPath, StatusCompra.CANCELADO);
                case SCHEDULED -> {
                    var scheduledStatuses = statusPath.in(StatusCompra.ORCAMENTO, StatusCompra.AGUARDANDO_EXECUCAO, StatusCompra.AGENDADA);
                    var notCancelled = cb.notEqual(statusPath, StatusCompra.CANCELADO);
                    var movementAfterToday = cb.greaterThan(movementDate, today);
                    yield cb.or(scheduledStatuses, cb.and(notCancelled, cb.not(scheduledStatuses), movementAfterToday));
                }
                case ACTIVE -> {
                    var notCancelled = cb.notEqual(statusPath, StatusCompra.CANCELADO);
                    var scheduledStatuses = statusPath.in(StatusCompra.ORCAMENTO, StatusCompra.AGUARDANDO_EXECUCAO, StatusCompra.AGENDADA);
                    yield cb.and(notCancelled, cb.not(scheduledStatuses), cb.lessThanOrEqualTo(movementDate, today));
                }
            };
        };
    }

    private Sort buildVendaSort(String sortBy, Sort.Direction direction) {
        String key = Optional.ofNullable(sortBy).orElse("movementDate");
        return switch (key) {
            case "amount" -> Sort.by(new Order(direction, "valorFinal"));
            case "description" -> Sort.by(new Order(direction, "sequencialUsuario"));
            case "createdAt" -> Sort.by(new Order(direction, "createdAt"));
            default -> JpaSort.unsafe(direction, "COALESCE(dataEfetuacao, dataAgendada)");
        };
    }

    private Sort buildCompraSort(String sortBy, Sort.Direction direction) {
        String key = Optional.ofNullable(sortBy).orElse("movementDate");
        return switch (key) {
            case "amount" -> Sort.by(new Order(direction, "valorFinal"));
            case "description" -> Sort.by(new Order(direction, "sequencialUsuario"));
            case "createdAt" -> Sort.by(new Order(direction, "createdAt"));
            default -> JpaSort.unsafe(direction, "COALESCE(dataEfetuacao, dataAgendada)");
        };
    }

    private String mapManualSortProperty(String sortBy) {
        return switch (Optional.ofNullable(sortBy).orElse("movementDate")) {
            case "amount" -> "amount";
            case "description" -> "description";
            case "createdAt" -> "createdAt";
            default -> "movementDate";
        };
    }

    private static class SourcePage {
        private final ArrayList<CashFlowEntryResponse> entries;
        private final long totalElements;

        private SourcePage(ArrayList<CashFlowEntryResponse> entries, long totalElements) {
            this.entries = entries;
            this.totalElements = totalElements;
        }

        static SourcePage empty() {
            return new SourcePage(new ArrayList<>(), 0L);
        }

        ArrayList<CashFlowEntryResponse> getEntries() {
            return entries;
        }

        long getTotalElements() {
            return totalElements;
        }
    }

    private CashFlowEntryResponse toCashFlowSale(Venda venda) {
        LocalDate movementDate = Optional.ofNullable(venda.getDataEfetuacao()).orElse(venda.getDataAgendada());
        CashFlowStatus status = mapSaleStatus(venda.getStatus(), movementDate);
        return CashFlowEntryResponse.builder()
                .id(venda.getId())
                .referenceId(venda.getId())
                .origin(CashFlowOrigin.SALE)
                .description(buildSaleDescription(venda))
                .amount(venda.getValorFinal())
                .type(CashFlowType.INCOME)
                .status(status)
                .movementDate(movementDate)
                .createdAt(venda.getCreatedAt())
                .createdBy(venda.getCreatedBy())
                .updatedAt(venda.getUpdatedAt())
                .updatedBy(venda.getUpdatedBy())
                .build();
    }

    private CashFlowEntryResponse toCashFlowPurchase(Compra compra) {
        LocalDate movementDate = Optional.ofNullable(compra.getDataEfetuacao()).orElse(compra.getDataAgendada());
        CashFlowStatus status = mapPurchaseStatus(compra.getStatus(), movementDate);
        return CashFlowEntryResponse.builder()
                .id(compra.getId())
                .referenceId(compra.getId())
                .origin(CashFlowOrigin.PURCHASE)
                .description(buildPurchaseDescription(compra))
                .amount(compra.getValorFinal())
                .type(CashFlowType.EXPENSE)
                .status(status)
                .movementDate(movementDate)
                .createdAt(compra.getCreatedAt())
                .createdBy(compra.getCreatedBy())
                .updatedAt(compra.getUpdatedAt())
                .updatedBy(compra.getUpdatedBy())
                .build();
    }

    private CashFlowStatus mapSaleStatus(StatusVenda statusVenda, LocalDate movementDate) {
        if (statusVenda == null) {
            return resolveStatus(movementDate);
        }
        return switch (statusVenda) {
            case CANCELADA -> CashFlowStatus.CANCELLED;
            case ORCAMENTO, PENDENTE, AGENDADA -> CashFlowStatus.SCHEDULED;
            default -> resolveStatus(movementDate);
        };
    }

    private CashFlowStatus mapPurchaseStatus(StatusCompra statusCompra, LocalDate movementDate) {
        if (statusCompra == null) {
            return resolveStatus(movementDate);
        }
        return switch (statusCompra) {
            case CANCELADO -> CashFlowStatus.CANCELLED;
            case ORCAMENTO, AGUARDANDO_EXECUCAO, AGENDADA -> CashFlowStatus.SCHEDULED;
            default -> resolveStatus(movementDate);
        };
    }

    private String buildSaleDescription(Venda venda) {
        String numero = Optional.ofNullable(venda.getSequencialUsuario())
                .map(seq -> "#" + seq)
                .orElse("Venda");
        String cliente = Optional.ofNullable(venda.getCliente())
                .map(clienteObj -> Optional.ofNullable(clienteObj.getNome()).orElse(null))
                .filter(name -> !name.isBlank())
                .orElse(null);
        if (cliente != null) {
            return String.format("Venda %s - %s", numero, cliente);
        }
        return String.format("Venda %s", numero);
    }

    private String buildPurchaseDescription(Compra compra) {
        String numero = Optional.ofNullable(compra.getSequencialUsuario())
                .map(seq -> "#" + seq)
                .orElse("Compra");
        String fornecedor = Optional.ofNullable(compra.getFornecedor())
                .map(fornecedorObj -> Optional.ofNullable(fornecedorObj.getNome()).orElse(null))
                .filter(name -> !name.isBlank())
                .orElse(null);
        if (fornecedor != null) {
            return String.format("Compra %s - %s", numero, fornecedor);
        }
        return String.format("Compra %s", numero);
    }

    private Integer getOrganizationId() {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return organizationId;
    }
}
