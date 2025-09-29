package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FornecedorMetricsService {

    private static final BigDecimal MINUTES_IN_DAY = BigDecimal.valueOf(1_440);

    private final CompraRepository compraRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional
    @CacheEvict(value = "fornecedores", allEntries = true)
    public void atualizarMetricasFornecedor(Integer fornecedorId, Integer organizationId) {
        if (fornecedorId == null || organizationId == null) {
            return;
        }

        fornecedorRepository.findByIdAndOrganizationId(fornecedorId, organizationId)
                .ifPresent(this::calcularMetricas);
    }

    private void calcularMetricas(Fornecedor fornecedor) {
        List<Compra> comprasElegiveis = compraRepository.findByFornecedorIdAndOrganizationIdAndStatusIn(
                fornecedor.getId(),
                fornecedor.getOrganizationId(),
                List.of(StatusCompra.CONCRETIZADO, StatusCompra.PAGO)
        );

        if (comprasElegiveis.isEmpty()) {
            fornecedor.setLeadTimeMedioDias(BigDecimal.ZERO);
            fornecedor.setTaxaEntregaNoPrazo(BigDecimal.ZERO);
            fornecedor.setCustoMedioPedido(BigDecimal.ZERO);
            fornecedorRepository.save(fornecedor);
            return;
        }

        BigDecimal totalLeadTimeDias = BigDecimal.ZERO;
        int comprasComLeadTime = 0;
        int totalAgendadas = 0;
        int concluidasNoPrazo = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (Compra compra : comprasElegiveis) {
            valorTotal = valorTotal.add(Optional.ofNullable(compra.getValorFinal()).orElse(BigDecimal.ZERO));

            LocalDate dataEfetuacao = compra.getDataEfetuacao();
            LocalDateTime dataConclusao = compra.getUpdatedAt();

            if (dataEfetuacao != null && dataConclusao != null) {
                Duration duracao = Duration.between(dataEfetuacao.atStartOfDay(), dataConclusao);
                long minutos = Math.max(duracao.toMinutes(), 0);
                BigDecimal dias = BigDecimal.valueOf(minutos)
                        .divide(MINUTES_IN_DAY, 4, RoundingMode.HALF_UP);
                totalLeadTimeDias = totalLeadTimeDias.add(dias);
                comprasComLeadTime++;
            }

            LocalDate dataAgendada = compra.getDataAgendada();
            if (dataAgendada != null && dataConclusao != null) {
                totalAgendadas++;
                if (!dataConclusao.toLocalDate().isAfter(dataAgendada)) {
                    concluidasNoPrazo++;
                }
            }
        }

        BigDecimal leadTimeMedio = comprasComLeadTime == 0
                ? BigDecimal.ZERO
                : totalLeadTimeDias.divide(BigDecimal.valueOf(comprasComLeadTime), 2, RoundingMode.HALF_UP);

        BigDecimal taxaEntregaNoPrazo = totalAgendadas == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(concluidasNoPrazo)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalAgendadas), 2, RoundingMode.HALF_UP);

        BigDecimal custoMedio = valorTotal.divide(BigDecimal.valueOf(comprasElegiveis.size()), 2, RoundingMode.HALF_UP);

        fornecedor.setLeadTimeMedioDias(leadTimeMedio);
        fornecedor.setTaxaEntregaNoPrazo(taxaEntregaNoPrazo);
        fornecedor.setCustoMedioPedido(custoMedio);

        fornecedorRepository.save(fornecedor);
    }
}
