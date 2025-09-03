package com.AIT.Optimanage.Repositories.Venda.Compatibilidade;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContextoCompatibilidadeRepository extends JpaRepository<ContextoCompatibilidade, Integer> {
    Optional<ContextoCompatibilidade> findByNome(String nome);
}