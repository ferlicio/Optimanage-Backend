package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorEnderecoRepository extends JpaRepository<FornecedorEndereco, Integer> {
    List<FornecedorEndereco> findAllByFornecedorIdAndFornecedorOwnerUser(Integer idFornecedor, User loggedUser);

    Optional<FornecedorEndereco> findByIdAndFornecedorIdAndFornecedorOwnerUser(Integer idEndereco, Integer idFornecedor, User loggedUser);
}
