package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorContatoRepository extends JpaRepository<FornecedorContato, Integer> {

    List<FornecedorContato> findAllByFornecedorId(Integer idFornecedor);

    Optional<FornecedorContato> findByIdAndFornecedorId(Integer idContato, Integer idFornecedor);
}
