package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.User.User;
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
        User loggedUser = CurrentUser.get();
        return pagamentoCompraRepository.findAllByCompraIdAndCompraOwnerUser(idCompra, loggedUser);
    }

    public List<CompraPagamento> listarPagamentosRealizadosCompra(Integer idCompra) {
        User loggedUser = CurrentUser.get();
        return pagamentoCompraRepository.findAllByCompraIdAndCompraOwnerUserAndStatusPagamento(idCompra, loggedUser, StatusPagamento.PAGO);
    }

    private CompraPagamento listarUmPagamentoCompra(Compra compra, Integer id) {
        User loggedUser = CurrentUser.get();
        return pagamentoCompraRepository.findByIdAndCompraAndCompraOwnerUser(id, compra, loggedUser)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public CompraPagamento listarUmPagamento(Integer idPagamento) {
        User loggedUser = CurrentUser.get();
        return pagamentoCompraRepository.findByIdAndCompraOwnerUser(idPagamento, loggedUser)
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
                .formaPagamento(pagamento.getFormaPagamento())
                .statusPagamento(pagamento.getStatusPagamento())
                .observacoes(pagamento.getObservacoes())
                .build();

        pagamentoCompraRepository.save(compraPagamento);
    }

    public CompraPagamento editarPagamento(Compra compra, PagamentoDTO pagamento, Integer idPagamento) {
        CompraPagamento compraPagamento = listarUmPagamentoCompra(compra, idPagamento);

        compraPagamento.setValorPago(pagamento.getValorPago());
        compraPagamento.setDataPagamento(pagamento.getDataPagamento());
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
