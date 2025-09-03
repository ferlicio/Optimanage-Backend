package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Venda.PagamentoVendaRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoVendaService {

    private final PagamentoVendaRepository pagamentoVendaRepository;
    private final VendaRepository vendaRepository;

    public List<VendaPagamento> listarPagamentosVenda(User loggedUser, Integer idVenda) {
        Venda venda = vendaRepository.findById(idVenda)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return pagamentoVendaRepository.findAllByVenda(venda);
    }

    public List<VendaPagamento> listarPagamentosRealizadosVenda(User loggedUser, Integer idVenda) {
        Venda venda = vendaRepository.findById(idVenda)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return pagamentoVendaRepository.findAllByVendaAndStatusPagamento(venda, StatusPagamento.PAGO);
    }

    private VendaPagamento listarUmPagamentoVenda(Venda venda, Integer idPagamento) {
        return pagamentoVendaRepository.findByIdAndVenda(idPagamento, venda)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public VendaPagamento listarUmPagamento(Venda venda, Integer idPagamento) {
        return listarUmPagamentoVenda(venda, idPagamento);
    }

    public void registrarPagamento(Venda venda, Integer idPagamento) {
        VendaPagamento pagamento = listarUmPagamentoVenda(venda, idPagamento);

        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.PAGO);

        pagamentoVendaRepository.save(pagamento);
    }

    public void lancarPagamento(Venda venda, PagamentoDTO pagamentoDTO) {

        VendaPagamento pagamento = VendaPagamento.builder()
                .venda(venda)
                .valorPago(pagamentoDTO.getValorPago())
                .dataPagamento(pagamentoDTO.getDataPagamento())
                .formaPagamento(pagamentoDTO.getFormaPagamento())
                .statusPagamento(pagamentoDTO.getStatusPagamento())
                .observacoes(pagamentoDTO.getObservacoes())
                .build();

        pagamentoVendaRepository.save(pagamento);
    }

    public void estornarPagamento(VendaPagamento pagamento) {
        VendaPagamento vendaPagamento = listarUmPagamento(pagamento.getVenda(), pagamento.getId());
        if (vendaPagamento.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new RuntimeException("O pagamento não pode ser estornado");
        }
        vendaPagamento.setStatusPagamento(StatusPagamento.ESTORNADO);
        pagamentoVendaRepository.save(vendaPagamento);
    }

}