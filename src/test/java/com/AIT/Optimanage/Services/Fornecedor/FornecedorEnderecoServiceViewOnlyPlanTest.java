package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorEnderecoRepository;
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
class FornecedorEnderecoServiceViewOnlyPlanTest {

    @Mock
    private FornecedorEnderecoRepository fornecedorEnderecoRepository;

    @Mock
    private FornecedorService fornecedorService;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private FornecedorEnderecoService fornecedorEnderecoService;

    @Test
    void cadastrarEnderecoLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(8);
        fornecedor.setTenantId(77);

        when(fornecedorService.listarUmFornecedor(8)).thenReturn(fornecedor);
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(77);

        FornecedorEndereco endereco = new FornecedorEndereco();

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> fornecedorEnderecoService.cadastrarEndereco(8, endereco));
    }
}
