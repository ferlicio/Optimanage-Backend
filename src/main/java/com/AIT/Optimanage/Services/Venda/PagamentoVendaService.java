package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoVendaService {

    private final PagamentoVendaRepository pagamentoVendaRepository;

    public List<VendaPagamento> listarPagamentosVenda(User loggedUser, Integer idVenda) {
        return pagamentoVendaRepository.findAllByVendaIdAndVendaOwnerUser(idVenda, loggedUser);
    }

    public List<VendaPagamento> listarPagamentosRealizadosVenda(User loggedUser, Integer idVenda) {
        return pagamentoVendaRepository.findAllByVendaIdAndVendaOwnerUserAndStatusPagamento(idVenda, loggedUser, StatusPagamento.PAGO);
    }

    private VendaPagamento listarUmPagamentoVenda(User loggedUser, Venda venda, Integer idPagamento) {
        return pagamentoVendaRepository.findByIdAndVendaAndVendaOwnerUser(idPagamento, venda, loggedUser)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public VendaPagamento listarUmPagamento(User loggedUser, Integer idPagamento) {
        return pagamentoVendaRepository.findByIdAndVendaOwnerUser(idPagamento, loggedUser)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public void registrarPagamento(User logedUser, Venda venda, Integer idPagamento) {
        VendaPagamento pagamento = listarUmPagamentoVenda(logedUser, venda, idPagamento);

        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.PAGO);
        pagamento.setDataVencimento(null);

        pagamentoVendaRepository.save(pagamento);
    }

    public void lancarPagamento(Venda venda, PagamentoDTO pagamentoDTO) {

        VendaPagamento pagamento = VendaPagamento.builder()
                .venda(venda)
                .valorPago(pagamentoDTO.getValorPago())
                .dataPagamento(pagamentoDTO.getDataPagamento())
                .dataVencimento(pagamentoDTO.getDataVencimento())
                .formaPagamento(pagamentoDTO.getFormaPagamento())
                .statusPagamento(pagamentoDTO.getStatusPagamento())
                .observacoes(pagamentoDTO.getObservacoes())
                .build();

        pagamentoVendaRepository.save(pagamento);
    }

    public void estornarPagamento(User logedUser, Integer idPagamento){
        VendaPagamento vendaPagamento = listarUmPagamento(logedUser, idPagamento);
        if (vendaPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("O pagamento não pode ser estornado");
        }
        vendaPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        vendaPagamento.setDataVencimento(null);
        pagamentoVendaRepository.save(vendaPagamento);
    }

    public void estornarPagamento(User logedUser, VendaPagamento pagamento) {
        VendaPagamento vendaPagamento = listarUmPagamento(logedUser, pagamento.getId());
        if (vendaPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("O pagamento não pode ser estornado");
        }
        vendaPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        vendaPagamento.setDataVencimento(null);
        pagamentoVendaRepository.save(vendaPagamento);
    }

}