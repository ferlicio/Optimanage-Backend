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
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowEntryRepository;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowFilters;
import com.AIT.Optimanage.Repositories.Compra.PagamentoCompraRepository;
import com.AIT.Optimanage.Repositories.FilterBuilder;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowEntryRepository repository;
    private final PagamentoVendaRepository pagamentoVendaRepository;
    private final PagamentoCompraRepository pagamentoCompraRepository;
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

        LocalDate today = LocalDate.now();

        var saleInstallments = listarParcelasVendas(organizationId, search, fetchSize, direction, sortBy, today);
        var purchaseInstallments = listarParcelasCompras(organizationId, search, fetchSize, direction, sortBy, today);

        long totalElements = manualPage.getTotalElements() + saleInstallments.totalElements()
                + purchaseInstallments.totalElements();

        var allEntries = Stream.of(manualEntries, saleInstallments.entries(), purchaseInstallments.entries())
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

    private String mapManualSortProperty(String sortBy) {
        return switch (Optional.ofNullable(sortBy).orElse("movementDate")) {
            case "amount" -> "amount";
            case "description" -> "description";
            case "createdAt" -> "createdAt";
            default -> "movementDate";
        };
    }

    private InstallmentResults listarParcelasVendas(Integer organizationId, CashFlowSearch search,
            int fetchSize, Sort.Direction direction, String sortBy, LocalDate today) {
        if (search.getType() != null && search.getType() != CashFlowType.INCOME) {
            return InstallmentResults.empty();
        }

        var installmentStatuses = buildInstallmentStatuses(search.getStatus());
        String sortKey = mapInstallmentSortKey(sortBy);

        Pageable pageable = PageRequest.of(0, Math.max(fetchSize, 1));

        var pagamentos = pagamentoVendaRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(
                organizationId,
                installmentStatuses,
                search.getStartDate(),
                search.getEndDate(),
                direction != Sort.Direction.DESC,
                sortKey,
                search.getStatus(),
                today,
                pageable);

        var entries = pagamentos.getContent().stream()
                .map(this::toCashFlowSaleInstallment)
                .filter(entry -> matchesStatusFilter(entry, search.getStatus()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new InstallmentResults(entries, pagamentos.getTotalElements());
    }

    private InstallmentResults listarParcelasCompras(Integer organizationId, CashFlowSearch search,
            int fetchSize, Sort.Direction direction, String sortBy, LocalDate today) {
        if (search.getType() != null && search.getType() != CashFlowType.EXPENSE) {
            return InstallmentResults.empty();
        }

        var installmentStatuses = buildInstallmentStatuses(search.getStatus());
        String sortKey = mapInstallmentSortKey(sortBy);

        Pageable pageable = PageRequest.of(0, Math.max(fetchSize, 1));

        var pagamentos = pagamentoCompraRepository.findInstallmentsByOrganizationAndStatusesAndDateRange(
                organizationId,
                installmentStatuses,
                search.getStartDate(),
                search.getEndDate(),
                direction != Sort.Direction.DESC,
                sortKey,
                search.getStatus(),
                today,
                pageable);

        var entries = pagamentos.getContent().stream()
                .map(this::toCashFlowPurchaseInstallment)
                .filter(entry -> matchesStatusFilter(entry, search.getStatus()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new InstallmentResults(entries, pagamentos.getTotalElements());
    }

    private record InstallmentResults(ArrayList<CashFlowEntryResponse> entries, long totalElements) {
        static InstallmentResults empty() {
            return new InstallmentResults(new ArrayList<>(), 0L);
        }
    }

    private String mapInstallmentSortKey(String sortBy) {
        return switch (Optional.ofNullable(sortBy).orElse("movementDate")) {
            case "amount" -> InstallmentSortKey.AMOUNT.name();
            case "description" -> InstallmentSortKey.DESCRIPTION.name();
            case "createdAt" -> InstallmentSortKey.CREATED_AT.name();
            default -> InstallmentSortKey.MOVEMENT_DATE.name();
        };
    }

    private enum InstallmentSortKey {
        MOVEMENT_DATE,
        AMOUNT,
        DESCRIPTION,
        CREATED_AT
    }

    private List<StatusPagamento> buildInstallmentStatuses(CashFlowStatus filter) {
        if (filter == CashFlowStatus.CANCELLED) {
            return List.of(StatusPagamento.ESTORNADO);
        }
        if (filter == CashFlowStatus.SCHEDULED) {
            return List.of(StatusPagamento.PENDENTE);
        }
        return List.of(StatusPagamento.PAGO, StatusPagamento.PENDENTE);
    }

    private CashFlowEntryResponse toCashFlowSaleInstallment(VendaPagamento pagamento) {
        LocalDate movementDate = resolveInstallmentMovementDate(pagamento.getStatusPagamento(),
                pagamento.getDataPagamento(), pagamento.getDataVencimento());
        CashFlowStatus status = mapInstallmentStatus(pagamento.getStatusPagamento(), movementDate);
        Venda venda = pagamento.getVenda();
        return CashFlowEntryResponse.builder()
                .id(pagamento.getId())
                .referenceId(venda != null ? venda.getId() : null)
                .origin(CashFlowOrigin.SALE_INSTALLMENT)
                .description(buildSaleInstallmentDescription(pagamento))
                .amount(pagamento.getValorPago())
                .type(CashFlowType.INCOME)
                .status(status)
                .movementDate(movementDate)
                .createdAt(pagamento.getCreatedAt())
                .createdBy(pagamento.getCreatedBy())
                .updatedAt(pagamento.getUpdatedAt())
                .updatedBy(pagamento.getUpdatedBy())
                .build();
    }

    private CashFlowEntryResponse toCashFlowPurchaseInstallment(CompraPagamento pagamento) {
        LocalDate movementDate = resolveInstallmentMovementDate(pagamento.getStatusPagamento(),
                pagamento.getDataPagamento(), pagamento.getDataVencimento());
        CashFlowStatus status = mapInstallmentStatus(pagamento.getStatusPagamento(), movementDate);
        return CashFlowEntryResponse.builder()
                .id(pagamento.getId())
                .referenceId(pagamento.getCompra() != null ? pagamento.getCompra().getId() : null)
                .origin(CashFlowOrigin.PURCHASE_INSTALLMENT)
                .description(buildPurchaseInstallmentDescription(pagamento))
                .amount(pagamento.getValorPago())
                .type(CashFlowType.EXPENSE)
                .status(status)
                .movementDate(movementDate)
                .createdAt(pagamento.getCreatedAt())
                .createdBy(pagamento.getCreatedBy())
                .updatedAt(pagamento.getUpdatedAt())
                .updatedBy(pagamento.getUpdatedBy())
                .build();
    }

    private LocalDate resolveInstallmentMovementDate(StatusPagamento statusPagamento, LocalDate dataPagamento,
                                                     LocalDate dataVencimento) {
        if (statusPagamento == StatusPagamento.PAGO && dataPagamento != null) {
            return dataPagamento;
        }
        return dataVencimento;
    }

    private CashFlowStatus mapInstallmentStatus(StatusPagamento statusPagamento, LocalDate movementDate) {
        if (statusPagamento == null) {
            return resolveStatus(movementDate);
        }
        return switch (statusPagamento) {
            case ESTORNADO -> CashFlowStatus.CANCELLED;
            case PAGO -> CashFlowStatus.ACTIVE;
            case PENDENTE -> resolveStatus(movementDate);
        };
    }

    private boolean matchesStatusFilter(CashFlowEntryResponse entry, CashFlowStatus filter) {
        if (filter == null) {
            return entry.getStatus() != CashFlowStatus.CANCELLED;
        }
        return entry.getStatus() == filter;
    }

    private String buildSaleInstallmentDescription(VendaPagamento pagamento) {
        String base = buildSaleDescription(pagamento.getVenda());
        if (base == null || base.isBlank()) {
            return "Parcela de venda";
        }
        return base + " - Parcela";
    }

    private String buildPurchaseInstallmentDescription(CompraPagamento pagamento) {
        String base = buildPurchaseDescription(pagamento.getCompra());
        if (base == null || base.isBlank()) {
            return "Parcela de compra";
        }
        return base + " - Parcela";
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
