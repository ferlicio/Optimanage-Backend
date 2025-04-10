package com.AIT.Optimanage.Repositories.User;

import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContadorRepository extends JpaRepository<Contador, Integer> {

    Contador getByNomeTabelaAndOwnerUser(Tabela nomeTabela, User loggedUser);
}
