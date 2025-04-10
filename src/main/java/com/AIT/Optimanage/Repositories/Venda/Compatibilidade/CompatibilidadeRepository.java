package com.AIT.Optimanage.Repositories.Venda.Compatibilidade;

import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompatibilidadeRepository extends JpaRepository<Compatibilidade, Integer> {

    List<Compatibilidade> findByContextoAndConfirmadoIsTrue(ContextoCompatibilidade contexto);
}
