package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendaServicoRepository extends JpaRepository<VendaServico, Integer> {

    void deleteByVenda(Venda venda);
}
