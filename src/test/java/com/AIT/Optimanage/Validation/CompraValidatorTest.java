package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompraValidatorTest {

    private final CompraValidator validator = new CompraValidator();

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void validarCompraSemItensLancaMensagemEspecificaParaCompra() {
        CompraDTO dto = CompraDTO.builder()
                .fornecedorId(1)
                .dataEfetuacao(LocalDate.now())
                .status(StatusCompra.AGUARDANDO_EXECUCAO)
                .produtos(List.of())
                .servicos(List.of())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validarCompra(dto));

        assertEquals("Uma compra deve ter no mínimo um produto ou serviço", exception.getMessage());
    }
}
