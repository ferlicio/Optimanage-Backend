package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorEnderecoRepository extends JpaRepository<FornecedorEndereco, Integer> {
    List<FornecedorEndereco> findAllByFornecedor_IdAndFornecedorOrganizationId(Integer idFornecedor, Integer organizationId);

    Optional<FornecedorEndereco> findByIdAndFornecedor_IdAndFornecedorOrganizationId(Integer idEndereco, Integer idFornecedor, Integer organizationId);
}
