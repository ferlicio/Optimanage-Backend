package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CompraValidator {
    public void validarCompra(CompraDTO compraDTO) {
        User loggedUser = CurrentUser.get();
        if (compraDTO.getDataEfetuacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efetuação não pode ser no futuro");
        }
        if (compraDTO.getDataAgendada() == null && compraDTO.getStatus() == StatusCompra.AGENDADA) {
            throw new IllegalArgumentException("Data agendada não informada para compra agendada");
        }
        if (compraDTO.getDataCobranca() == null) {
            if (compraDTO.getStatus() == StatusCompra.AGUARDANDO_PAG) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda aguardando pagamento");
            } else if (compraDTO.getStatus() == StatusCompra.CONCRETIZADO) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda concretizada");
            }
        }
        if (compraDTO.getStatus() == null) {
            throw new IllegalArgumentException("Status não informado");
        } else if (compraDTO.getStatus() == StatusCompra.ORCAMENTO && !loggedUser.getUserInfo().getPermiteOrcamento()) {
            throw new IllegalArgumentException("Usuário não tem permissão para criar orçamentos");
        }
        if (compraDTO.hasNoItems()) {
            throw new IllegalArgumentException("Uma venda deve ter no mínimo um produto ou serviço");
        }
    }
}
