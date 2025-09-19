package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Repositories.User.ContadorRepository;
import com.AIT.Optimanage.Security.CurrentUser;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContadorService {

    private final ContadorRepository contadorRepository;

    public Contador BuscarContador(Tabela tabela) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return contadorRepository.findByNomeTabelaAndOrganizationId(tabela, organizationId)
                .orElseGet(() -> {
                    Contador contador = Contador.builder()
                            .nomeTabela(tabela)
                            .contagemAtual(1)
                            .build();
                    contador.setTenantId(organizationId);
                    return contadorRepository.save(contador);
                });
    }

    public void IncrementarContador(Tabela tabela) {
        Contador contador = BuscarContador(tabela);
        contador.setContagemAtual(contador.getContagemAtual() + 1);
        contadorRepository.save(contador);
    }
}
