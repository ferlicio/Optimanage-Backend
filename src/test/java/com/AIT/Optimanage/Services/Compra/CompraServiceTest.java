package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Mappers.CompraMapper;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraResponseDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Compra.CompraProdutoRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraServicoRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Services.Compra.PagamentoCompraService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock private CompraRepository compraRepository;
    @Mock private FornecedorService fornecedorService;
    @Mock private ContadorService contadorService;
    @Mock private ProdutoService produtoService;
    @Mock private ServicoService servicoService;
    @Mock private CompraProdutoRepository compraProdutoRepository;
    @Mock private CompraServicoRepository compraServicoRepository;
    @Mock private PagamentoCompraService pagamentoCompraService;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private CompraMapper compraMapper;

    @InjectMocks
    private CompraService compraService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder().build();
        user.setId(1);
        CurrentUser.set(user);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void agendarCompraAtualizaStatusEData() {
        Compra compra = Compra.builder()
                .ownerUser(user)
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.valueOf(100))
                .status(StatusCompra.AGUARDANDO_EXECUCAO)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(10);

        when(compraRepository.findByIdAndOwnerUser(10, user)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.agendarCompra(10, "2024-05-01");

        assertEquals(StatusCompra.AGENDADA, compra.getStatus());
        assertEquals(LocalDate.parse("2024-05-01"), compra.getDataAgendada());
    }

    @Test
    void finalizarAgendamentoCompraSemPagamentoDefineStatusAguardandoPag() {
        Compra compra = Compra.builder()
                .ownerUser(user)
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.valueOf(100))
                .status(StatusCompra.AGENDADA)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(11);

        when(compraRepository.findByIdAndOwnerUser(11, user)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.finalizarAgendamentoCompra(11);

        assertEquals(StatusCompra.AGUARDANDO_PAG, compra.getStatus());
    }

    @Test
    void finalizarAgendamentoCompraComPagamentoCompletoDefineStatusConcretizado() {
        Compra compra = Compra.builder()
                .ownerUser(user)
                .sequencialUsuario(1)
                .dataEfetuacao(LocalDate.now())
                .valorFinal(BigDecimal.valueOf(100))
                .valorPendente(BigDecimal.ZERO)
                .status(StatusCompra.AGENDADA)
                .compraProdutos(Collections.singletonList(new CompraProduto()))
                .build();
        compra.setId(12);

        when(compraRepository.findByIdAndOwnerUser(12, user)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(compraMapper.toResponse(compra)).thenReturn(new CompraResponseDTO());

        compraService.finalizarAgendamentoCompra(12);

        assertEquals(StatusCompra.CONCRETIZADO, compra.getStatus());
    }
}

