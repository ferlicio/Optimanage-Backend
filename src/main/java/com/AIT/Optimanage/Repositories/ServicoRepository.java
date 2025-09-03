package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Page<Servico> findAllByAtivoTrue(Pageable pageable);

    Optional<Servico> findByIdAndAtivoTrue(Integer idServico);
}
