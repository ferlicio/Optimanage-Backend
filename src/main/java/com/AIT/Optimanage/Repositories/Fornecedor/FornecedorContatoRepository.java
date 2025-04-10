package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorContatoRepository extends JpaRepository<FornecedorContato, Integer> {

    List<FornecedorContato> findAllByFornecedorIdAndFornecedorOwnerUser(Integer idFornecedor, User loggedUser);

    Optional<FornecedorContato>  findByIdAndFornecedorIdAndFornecedorOwnerUser(Integer idContato, Integer idFornecedor, User loggedUser);
}
