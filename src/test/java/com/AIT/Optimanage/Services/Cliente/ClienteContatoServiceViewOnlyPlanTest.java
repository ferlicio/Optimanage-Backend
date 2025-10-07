package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Cliente.ClienteContato;
import com.AIT.Optimanage.Repositories.Cliente.ClienteContatoRepository;
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
class ClienteContatoServiceViewOnlyPlanTest {

    @Mock
    private ClienteContatoRepository clienteContatoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private ClienteContatoService clienteContatoService;

    @Test
    void cadastrarContatoLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        cliente.setTenantId(55);

        when(clienteService.listarUmCliente(10)).thenReturn(cliente);
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(55);

        ClienteContato contato = new ClienteContato();

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> clienteContatoService.cadastrarContato(10, contato));
    }
}
