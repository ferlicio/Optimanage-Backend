package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceViewOnlyPlanTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private com.AIT.Optimanage.Mappers.ProdutoMapper produtoMapper;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @Mock
    private PlanoService planoService;

    @InjectMocks
    private ProdutoService produtoService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(42);
        user.setTenantId(77);
        CurrentUser.set(user);
        TenantContext.setTenantId(77);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
        TenantContext.clear();
    }

    @Test
    void cadastrarProdutoLancaExcecaoQuandoPlanoEhSomenteVisualizacao() {
        doThrow(new PlanoSomenteVisualizacaoException()).when(planoAccessGuard).garantirPermissaoDeEscrita(77);

        ProdutoRequest request = ProdutoRequest.builder()
                .sequencialUsuario(1)
                .codigoReferencia("SKU")
                .nome("Produto")
                .custo(java.math.BigDecimal.ZERO)
                .valorVenda(java.math.BigDecimal.ZERO)
                .qtdEstoque(0)
                .build();

        assertThrows(PlanoSomenteVisualizacaoException.class, () -> produtoService.cadastrarProduto(request));
    }
}
