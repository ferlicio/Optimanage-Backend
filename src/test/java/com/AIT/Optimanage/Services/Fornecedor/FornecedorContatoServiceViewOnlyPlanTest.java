package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorContatoRepository;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorContatoServiceViewOnlyPlanTest {

    @Mock
    private FornecedorContatoRepository fornecedorContatoRepository;

    @Mock
    private FornecedorService fornecedorService;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private FornecedorContatoService fornecedorContatoService;

    @Test
    void cadastrarContatoLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(5);
        fornecedor.setTenantId(33);

        when(fornecedorService.listarUmFornecedor(5)).thenReturn(fornecedor);
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(33);

        FornecedorContato contato = new FornecedorContato();

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> fornecedorContatoService.cadastrarContato(5, contato));
    }
}
