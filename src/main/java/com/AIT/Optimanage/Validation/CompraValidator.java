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
        boolean permiteOrcamento = loggedUser != null
                && loggedUser.getOrganization() != null
                && Boolean.TRUE.equals(loggedUser.getOrganization().getPermiteOrcamento());
        if (compraDTO.getStatus() == null) {
            throw new IllegalArgumentException("Status não informado");
        } else if (compraDTO.getStatus() == StatusCompra.ORCAMENTO && !permiteOrcamento) {
            throw new IllegalArgumentException("Usuário não tem permissão para criar orçamentos");
        }
        if (compraDTO.hasNoItems()) {
            throw new IllegalArgumentException("Uma compra deve ter no mínimo um produto ou serviço");
        }
    }
}
