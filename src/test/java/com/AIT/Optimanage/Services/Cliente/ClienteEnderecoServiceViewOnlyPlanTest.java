package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteEndereco;
import com.AIT.Optimanage.Repositories.Cliente.ClienteEnderecoRepository;
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
class ClienteEnderecoServiceViewOnlyPlanTest {

    @Mock
    private ClienteEnderecoRepository clienteEnderecoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private ClienteEnderecoService clienteEnderecoService;

    @Test
    void cadastrarEnderecoLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        Cliente cliente = new Cliente();
        cliente.setId(7);
        cliente.setTenantId(91);

        when(clienteService.listarUmCliente(7)).thenReturn(cliente);
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(91);

        ClienteEndereco endereco = new ClienteEndereco();

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> clienteEnderecoService.cadastrarEndereco(7, endereco));
    }
}
