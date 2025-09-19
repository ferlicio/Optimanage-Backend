package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Page<Servico> findAllByOrganizationIdAndAtivoTrue(Integer organizationId, Pageable pageable);

    Optional<Servico> findByIdAndOrganizationIdAndAtivoTrue(Integer idServico, Integer organizationId);

    Optional<Servico> findByIdAndOrganizationId(Integer idServico, Integer organizationId);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);
}
