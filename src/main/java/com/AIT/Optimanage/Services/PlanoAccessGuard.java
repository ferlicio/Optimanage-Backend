package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanoAccessGuard {

    private final PlanoService planoService;

    public void garantirPermissaoDeEscrita(Integer organizationId) {
        if (organizationId == null) {
            return;
        }

        User tenantProbe = new User();
        tenantProbe.setTenantId(organizationId);

        Plano planoAtual = planoService.obterPlanoUsuario(tenantProbe).orElse(null);
        if (planoService.isPlanoSomenteVisualizacao(planoAtual)) {
            throw new PlanoSomenteVisualizacaoException();
        }
    }
}
