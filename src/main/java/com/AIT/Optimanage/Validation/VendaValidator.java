package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VendaValidator {
    public void validarVenda(VendaDTO vendaDTO, User loggedUser) {
        if (vendaDTO.getDataEfetuacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efetuação não pode ser no futuro");
        }
        if (vendaDTO.getDataAgendada() == null && vendaDTO.getStatus() == StatusVenda.AGENDADA) {
            throw new IllegalArgumentException("Data agendada não informada para venda agendada");
        }
        if (vendaDTO.getDataCobranca() == null) {
            if (vendaDTO.getStatus() == StatusVenda.AGUARDANDO_PAG) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda aguardando pagamento");
            } else if (vendaDTO.getStatus() == StatusVenda.CONCRETIZADA) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda concretizada");
            }
        }
        if (vendaDTO.getStatus() == null) {
            throw new IllegalArgumentException("Status não informado");
        } else if (vendaDTO.getStatus() == StatusVenda.ORCAMENTO && !loggedUser.getUserInfo().getPermiteOrcamento()) {
            throw new IllegalArgumentException("Usuário não tem permissão para criar orçamentos");
        }
        if (vendaDTO.hasNoItems()) {
            throw new IllegalArgumentException("Uma venda deve ter no mínimo um produto ou serviço");
        }
    }
}
