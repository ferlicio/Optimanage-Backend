package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Repositories.InventoryHistoryRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceViewOnlyPlanTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private InventoryHistoryRepository historyRepository;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    @InjectMocks
    private InventoryService inventoryService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void incrementarManualLancaExcecaoQuandoPlanoSomenteVisualizacao() {
        TenantContext.setTenantId(55);
        doThrow(new PlanoSomenteVisualizacaoException())
                .when(planoAccessGuard).garantirPermissaoDeEscrita(55);

        assertThrows(PlanoSomenteVisualizacaoException.class,
                () -> inventoryService.incrementar(10, 5));

        verify(planoAccessGuard).garantirPermissaoDeEscrita(55);
    }
}
