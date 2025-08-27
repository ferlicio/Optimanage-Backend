package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    List<Servico> findAllByOwnerUserAndAtivoTrue(User ownerUser);

    Optional<Servico> findByIdAndOwnerUserAndAtivoTrue(Integer idServico, User ownerUser);

    Optional<Servico> findByIdAndOwnerUser(Integer idServico, User ownerUser);
}
