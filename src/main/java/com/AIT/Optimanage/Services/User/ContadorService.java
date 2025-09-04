package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Repositories.User.ContadorRepository;
import com.AIT.Optimanage.Security.CurrentUser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContadorService {

    private final ContadorRepository contadorRepository;

    public Contador BuscarContador(Tabela tabela) {
        Contador contador = contadorRepository.getByNomeTabelaAndOwnerUser(tabela, CurrentUser.get());
        if (contador == null) {
            return contadorRepository.save(Contador.builder()
                    .nomeTabela(tabela)
                    .contagemAtual(1)
                    .build()
            );
        }
        return contador;
    }

    public void IncrementarContador(Tabela tabela) {
        Contador contador = contadorRepository.getByNomeTabelaAndOwnerUser(tabela, CurrentUser.get());
        contador.setContagemAtual(contador.getContagemAtual() + 1);
        contadorRepository.save(contador);
    }
}
