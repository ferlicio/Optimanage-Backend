package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoVendaService {

    private final PagamentoVendaRepository pagamentoVendaRepository;
    private final PlanoAccessGuard planoAccessGuard;

    public List<VendaPagamento> listarPagamentosVenda(User loggedUser, Integer idVenda) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoVendaRepository.findAllByVendaIdAndVendaOrganizationId(idVenda, organizationId);
    }

    public List<VendaPagamento> listarPagamentosRealizadosVenda(User loggedUser, Integer idVenda) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoVendaRepository.findAllByVendaIdAndVendaOrganizationIdAndStatusPagamento(idVenda, organizationId, StatusPagamento.PAGO);
    }

    private VendaPagamento listarUmPagamentoVenda(User loggedUser, Venda venda, Integer idPagamento) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoVendaRepository.findByIdAndVendaAndVendaOrganizationId(idPagamento, venda, organizationId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public VendaPagamento listarUmPagamento(User loggedUser, Integer idPagamento) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return pagamentoVendaRepository.findByIdAndVendaOrganizationId(idPagamento, organizationId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public void registrarPagamento(User logedUser, Venda venda, Integer idPagamento) {
        garantirEscrita(logedUser, venda);
        VendaPagamento pagamento = listarUmPagamentoVenda(logedUser, venda, idPagamento);

        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.PAGO);

        pagamentoVendaRepository.save(pagamento);
    }

    public void lancarPagamento(Venda venda, PagamentoDTO pagamentoDTO) {
        garantirEscrita(venda);

        VendaPagamento pagamento = VendaPagamento.builder()
                .venda(venda)
                .valorPago(pagamentoDTO.getValorPago())
                .dataPagamento(pagamentoDTO.getDataPagamento())
                .dataVencimento(pagamentoDTO.getDataVencimento())
                .formaPagamento(pagamentoDTO.getFormaPagamento())
                .statusPagamento(pagamentoDTO.getStatusPagamento())
                .observacoes(pagamentoDTO.getObservacoes())
                .build();

        pagamento.setTenantId(venda.getOrganizationId());
        pagamentoVendaRepository.save(pagamento);
    }

    public void estornarPagamento(User logedUser, Integer idPagamento){
        VendaPagamento vendaPagamento = listarUmPagamento(logedUser, idPagamento);
        garantirEscrita(logedUser, vendaPagamento.getVenda());
        if (vendaPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("O pagamento não pode ser estornado");
        }
        vendaPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        pagamentoVendaRepository.save(vendaPagamento);
    }

    public void estornarPagamento(User logedUser, VendaPagamento pagamento) {
        VendaPagamento vendaPagamento = listarUmPagamento(logedUser, pagamento.getId());
        garantirEscrita(logedUser, vendaPagamento.getVenda());
        if (vendaPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("O pagamento não pode ser estornado");
        }
        vendaPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        pagamentoVendaRepository.save(vendaPagamento);
    }

    private void garantirEscrita(User loggedUser, Venda venda) {
        Integer organizationId = resolveOrganizationId(loggedUser, venda);
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
    }

    private void garantirEscrita(Venda venda) {
        Integer organizationId = venda != null ? venda.getOrganizationId() : null;
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
    }

    private Integer resolveOrganizationId(User loggedUser, Venda venda) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null && venda != null) {
            organizationId = venda.getOrganizationId();
        }
        if (organizationId == null) {
            throw new RuntimeException("Organização não encontrada");
        }
        return organizationId;
    }

}