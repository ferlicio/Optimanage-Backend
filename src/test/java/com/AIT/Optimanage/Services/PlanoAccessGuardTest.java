package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanoAccessGuardTest {

    @Mock
    private PlanoService planoService;

    private PlanoAccessGuard guard;

    @BeforeEach
    void setUp() {
        guard = new PlanoAccessGuard(planoService);
    }

    @Test
    void garantirPermissaoDeEscritaNaoLancaQuandoPlanoNaoEhSomenteVisualizacao() {
        Plano plano = new Plano();
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.of(plano));
        when(planoService.isPlanoSomenteVisualizacao(plano)).thenReturn(false);

        assertDoesNotThrow(() -> guard.garantirPermissaoDeEscrita(10));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(planoService).obterPlanoUsuario(captor.capture());
        assertEquals(10, captor.getValue().getOrganizationId());
        verify(planoService).isPlanoSomenteVisualizacao(plano);
    }

    @Test
    void garantirPermissaoDeEscritaLancaQuandoPlanoEhSomenteVisualizacao() {
        Plano plano = new Plano();
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.of(plano));
        when(planoService.isPlanoSomenteVisualizacao(plano)).thenReturn(true);

        assertThrows(PlanoSomenteVisualizacaoException.class, () -> guard.garantirPermissaoDeEscrita(5));
    }

    @Test
    void garantirPermissaoDeEscritaIgnoraQuandoPlanoNaoEncontrado() {
        when(planoService.obterPlanoUsuario(any(User.class))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> guard.garantirPermissaoDeEscrita(7));
        ArgumentCaptor<Plano> planoCaptor = ArgumentCaptor.forClass(Plano.class);
        verify(planoService).isPlanoSomenteVisualizacao(planoCaptor.capture());
        assertNull(planoCaptor.getValue());
    }
}
