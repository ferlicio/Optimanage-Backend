package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorContatoRepository extends JpaRepository<FornecedorContato, Integer> {

    List<FornecedorContato> findAllByFornecedor_Id(Integer idFornecedor);

    Optional<FornecedorContato>  findByIdAndFornecedor_Id(Integer idContato, Integer idFornecedor);
}
