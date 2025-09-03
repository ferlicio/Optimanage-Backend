package com.AIT.Optimanage.Services.User;

import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.User.ContadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContadorService {

    private final ContadorRepository contadorRepository;

    public Contador BuscarContador(Tabela tabela, User loggedUser) {
        Contador contador = contadorRepository.getByNomeTabelaAndOwnerUser(tabela, loggedUser);
        if (contador == null) {
            return contadorRepository.save(Contador.builder()
                    .nomeTabela(tabela)
                    .contagemAtual(1)
                    .build()
            );
        }
        return contador;
    }

    public void IncrementarContador(Tabela tabela, User ownerUser) {
        Contador contador = contadorRepository.getByNomeTabelaAndOwnerUser(tabela, ownerUser);
        contador.setContagemAtual(contador.getContagemAtual() + 1);
        contadorRepository.save(contador);
    }
}
