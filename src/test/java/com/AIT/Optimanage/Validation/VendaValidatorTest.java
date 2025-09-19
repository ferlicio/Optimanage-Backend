package com.AIT.Optimanage.Validation;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VendaValidatorTest {

    private final VendaValidator validator = new VendaValidator();

    @Test
    void validarVendaAceitaDescontosNoLimite() {
        VendaDTO dto = VendaDTO.builder()
                .clienteId(1)
                .dataEfetuacao(LocalDate.now())
                .dataCobranca(LocalDate.now())
                .status(StatusVenda.PENDENTE)
                .descontoGeral(new BigDecimal("100.0"))
                .produtos(List.of(new VendaProdutoDTO(1, 1, new BigDecimal("0"))))
                .servicos(List.of(new VendaServicoDTO(1, 1, new BigDecimal("100"))))
                .build();

        assertDoesNotThrow(() -> validator.validarVenda(dto, new User()));
    }

    @Test
    void validarVendaRejeitaDescontoGeralMaiorQueCem() {
        VendaDTO dto = VendaDTO.builder()
                .clienteId(1)
                .dataEfetuacao(LocalDate.now())
                .dataCobranca(LocalDate.now())
                .status(StatusVenda.PENDENTE)
                .descontoGeral(new BigDecimal("100.01"))
                .produtos(List.of(new VendaProdutoDTO(1, 1, BigDecimal.ZERO)))
                .servicos(List.of(new VendaServicoDTO(1, 1, BigDecimal.ZERO)))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarVenda(dto, new User()));
    }

    @Test
    void validarVendaRejeitaDescontoDeProdutoForaDaFaixa() {
        VendaDTO dto = VendaDTO.builder()
                .clienteId(1)
                .dataEfetuacao(LocalDate.now())
                .dataCobranca(LocalDate.now())
                .status(StatusVenda.PENDENTE)
                .descontoGeral(BigDecimal.ZERO)
                .produtos(List.of(new VendaProdutoDTO(1, 1, new BigDecimal("150"))))
                .servicos(List.of(new VendaServicoDTO(1, 1, BigDecimal.ZERO)))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarVenda(dto, new User()));
    }

    @Test
    void validarVendaRejeitaDescontoDeServicoNegativo() {
        VendaDTO dto = VendaDTO.builder()
                .clienteId(1)
                .dataEfetuacao(LocalDate.now())
                .dataCobranca(LocalDate.now())
                .status(StatusVenda.PENDENTE)
                .descontoGeral(BigDecimal.ZERO)
                .produtos(List.of(new VendaProdutoDTO(1, 1, BigDecimal.ZERO)))
                .servicos(List.of(new VendaServicoDTO(1, 1, new BigDecimal("-0.5"))))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarVenda(dto, new User()));
    }
}
