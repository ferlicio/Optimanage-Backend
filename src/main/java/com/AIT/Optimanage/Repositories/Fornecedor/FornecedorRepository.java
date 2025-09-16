package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {


    @Query("SELECT DISTINCT f FROM Fornecedor f " +
            "LEFT JOIN f.enderecos e " +
            "WHERE " +
            "(:userId IS NULL OR f.ownerUser.id = :userId) AND " +
            "(:id IS NULL OR f.id = :id) OR " +
            "(:userId IS NULL OR f.ownerUser.id = :userId) AND " +
            "(:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
            "OR LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:cpfOuCnpj IS NULL OR " +
            " REPLACE(REPLACE(REPLACE(f.cpf, '.', ''), '-', ''), '/', '') = REPLACE(REPLACE(REPLACE(:cpfOuCnpj, '.', ''), '-', ''), '/', '') " +
            " OR REPLACE(REPLACE(REPLACE(f.cnpj, '.', ''), '-', ''), '/', '') = REPLACE(REPLACE(REPLACE(:cpfOuCnpj, '.', ''), '-', ''), '/', '')) AND " +
            "(:atividade IS NULL OR f.atividade.id = :atividade) AND " +
            "(:estado IS NULL OR EXISTS (SELECT 1 FROM ClienteEndereco e WHERE e.cliente.id = f.id AND e.estado = :estado)) AND " +
            "(:tipoPessoa IS NULL OR f.tipoPessoa = :tipoPessoa) AND " +
            "(:ativo IS NULL OR f.ativo = :ativo)")
    Page<Fornecedor> buscarFornecedores(
            @Param("userId") Integer userId,
            @Param("id") Integer id,
            @Param("nome") String nome,
            @Param("cpfOuCnpj") String cpfOuCnpj,
            @Param("atividade") Integer atividade,
            @Param("estado") String estado,
            @Param("tipoPessoa") TipoPessoa tipoPessoa,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );

    Optional<Fornecedor> findByIdAndOwnerUserAndAtivoTrue(Integer idFornecedor, User loggedUser);

    Optional<Fornecedor> findByIdAndOwnerUserAndAtivoFalse(Integer idFornecedor, User loggedUser);

    Optional<Fornecedor> findByIdAndOwnerUser(Integer idFornecedor, User loggedUser);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);
}
