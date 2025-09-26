package com.AIT.Optimanage.Services.CashFlow;

import com.AIT.Optimanage.Mappers.CashFlowMapper;
import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryRequest;
import com.AIT.Optimanage.Models.CashFlow.DTOs.CashFlowEntryResponse;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Search.CashFlowSearch;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowEntryRepository;
import com.AIT.Optimanage.Repositories.CashFlow.CashFlowFilters;
import com.AIT.Optimanage.Repositories.FilterBuilder;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowEntryRepository repository;
    private final CashFlowMapper mapper;

    @Transactional(readOnly = true)
    public Page<CashFlowEntryResponse> listarLancamentos(CashFlowSearch search) {
        Integer organizationId = getOrganizationId();

        Sort.Direction direction = Optional.ofNullable(search.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);
        String sortBy = Optional.ofNullable(search.getSort()).orElse("movementDate");

        int page = Optional.ofNullable(search.getPage()).orElse(0);
        int pageSize = Optional.ofNullable(search.getPageSize()).orElse(20);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        Specification<CashFlowEntry> spec = FilterBuilder
                .of(CashFlowFilters.hasOrganization(organizationId))
                .and(search.getType(), CashFlowFilters::hasType)
                .and(search.getStatus(), CashFlowFilters::hasStatus)
                .and(search.getStartDate(), CashFlowFilters::occursOnOrAfter)
                .and(search.getEndDate(), CashFlowFilters::occursOnOrBefore)
                .build();

        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CashFlowEntryResponse buscarLancamento(Integer id) {
        return mapper.toResponse(getEntry(id));
    }

    @Transactional
    public CashFlowEntryResponse criarLancamento(CashFlowEntryRequest request) {
        Integer organizationId = getOrganizationId();
        CashFlowEntry entry = mapper.toEntity(request);
        entry.setStatus(CashFlowStatus.ACTIVE);
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

    private Integer getOrganizationId() {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return organizationId;
    }
}
