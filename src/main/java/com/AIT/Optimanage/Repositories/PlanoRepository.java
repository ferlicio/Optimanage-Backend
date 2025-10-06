package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Integer> {

    Optional<Plano> findByNomeIgnoreCase(String nome);

    Optional<Plano> findByNomeIgnoreCaseAndOrganizationId(String nome, Integer organizationId);
}

