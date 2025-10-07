package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.User.ContadorRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContadorServiceViewOnlyPlanTest {

    @Mock
    private ContadorRepository contadorRepository;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private ContadorService contadorService;

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void buscarContadorCriaNovoLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        User user = new User();
        user.setTenantId(77);
        CurrentUser.set(user);

        when(contadorRepository.findByNomeTabelaAndOrganizationId(eq(Tabela.VENDA), eq(77)))
                .thenReturn(Optional.empty());
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(77);

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> contadorService.BuscarContador(Tabela.VENDA));

        verify(planoAccessGuard).garantirPermissaoDeEscrita(77);
        verify(contadorRepository, never()).save(any(Contador.class));
    }

    @Test
    void incrementarContadorLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        User user = new User();
        user.setTenantId(33);
        CurrentUser.set(user);

        Contador contador = Contador.builder()
                .nomeTabela(Tabela.PRODUTO)
                .contagemAtual(5)
                .build();
        contador.setTenantId(33);

        when(contadorRepository.findByNomeTabelaAndOrganizationId(eq(Tabela.PRODUTO), eq(33)))
                .thenReturn(Optional.of(contador));
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(33);

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> contadorService.IncrementarContador(Tabela.PRODUTO));

        verify(planoAccessGuard).garantirPermissaoDeEscrita(33);
        verify(contadorRepository, never()).save(contador);
    }
}
