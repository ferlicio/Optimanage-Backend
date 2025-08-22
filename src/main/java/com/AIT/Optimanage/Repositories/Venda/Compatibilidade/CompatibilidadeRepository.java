package com.AIT.Optimanage.Repositories.Venda.Compatibilidade;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompatibilidadeRepository extends JpaRepository<Compatibilidade, Integer> {

    List<Compatibilidade> findByContexto_IdAndCompativelIsTrue(Integer contextoId);
}
