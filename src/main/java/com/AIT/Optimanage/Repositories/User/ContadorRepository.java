package com.AIT.Optimanage.Repositories.User;

import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContadorRepository extends JpaRepository<Contador, Integer> {

    Optional<Contador> findByNomeTabelaAndOrganizationId(Tabela nomeTabela, Integer organizationId);
}
