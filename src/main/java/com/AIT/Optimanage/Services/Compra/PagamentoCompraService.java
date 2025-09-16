package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Repositories.Compra.PagamentoCompraRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoCompraService {

    private final PagamentoCompraRepository pagamentoCompraRepository;

    public List<CompraPagamento> listarPagamentosCompra(Integer idCompra) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoCompraRepository.findAllByCompraIdAndCompraOrganizationId(idCompra, organizationId);
    }

    public List<CompraPagamento> listarPagamentosRealizadosCompra(Integer idCompra) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoCompraRepository.findAllByCompraIdAndCompraOrganizationIdAndStatusPagamento(idCompra, organizationId, StatusPagamento.PAGO);
    }

    private CompraPagamento listarUmPagamentoCompra(Compra compra, Integer id) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        if (!organizationId.equals(compra.getOrganizationId())) {
            throw new RuntimeException("Compra não pertence à organização atual");
        }
        return pagamentoCompraRepository.findByIdAndCompraIdAndCompraOrganizationId(id, compra.getId(), organizationId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public CompraPagamento listarUmPagamento(Integer idPagamento) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoCompraRepository.findByIdAndCompraOrganizationId(idPagamento, organizationId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public void registrarPagamento(Compra compra, Integer idPagamento) {
        CompraPagamento pagamento = listarUmPagamentoCompra(compra , idPagamento);

        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.PAGO);

        pagamentoCompraRepository.save(pagamento);
    }

    public void lancarPagamento(Compra compra, PagamentoDTO pagamento) {

        CompraPagamento compraPagamento = CompraPagamento.builder()
                .compra(compra)
                .valorPago(pagamento.getValorPago())
                .dataPagamento(pagamento.getDataPagamento())
                .dataVencimento(pagamento.getDataVencimento())
                .formaPagamento(pagamento.getFormaPagamento())
                .statusPagamento(pagamento.getStatusPagamento())
                .observacoes(pagamento.getObservacoes())
                .build();

        compraPagamento.setTenantId(compra.getOrganizationId());
        pagamentoCompraRepository.save(compraPagamento);
    }

    public CompraPagamento editarPagamento(Compra compra, PagamentoDTO pagamento, Integer idPagamento) {
        CompraPagamento compraPagamento = listarUmPagamentoCompra(compra, idPagamento);

        compraPagamento.setValorPago(pagamento.getValorPago());
        compraPagamento.setDataPagamento(pagamento.getDataPagamento());
        compraPagamento.setDataVencimento(pagamento.getDataVencimento());
        compraPagamento.setFormaPagamento(pagamento.getFormaPagamento());
        compraPagamento.setStatusPagamento(pagamento.getStatusPagamento());
        compraPagamento.setObservacoes(pagamento.getObservacoes());

        return pagamentoCompraRepository.save(compraPagamento);
    }

    public void estornarPagamento(Integer idPagamento) {
        CompraPagamento compraPagamento = listarUmPagamento(idPagamento);
        if (compraPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("Pagamento não pode ser estornado");
        }
        compraPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        pagamentoCompraRepository.save(compraPagamento);
    }

    public void estornarPagamento(CompraPagamento pagamento) {
        CompraPagamento compraPagamento = listarUmPagamento(pagamento.getId());
        if (compraPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("Pagamento não pode ser estornado");
        }
        compraPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        pagamentoCompraRepository.save(compraPagamento);
    }
}
