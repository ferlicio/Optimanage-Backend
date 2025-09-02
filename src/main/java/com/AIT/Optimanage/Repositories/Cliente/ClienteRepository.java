package com.AIT.Optimanage.Repositories.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByAtivoTrue();

    @Query("SELECT DISTINCT c FROM Cliente c " +
            "LEFT JOIN c.enderecos e " +
            "WHERE (:id IS NULL OR c.id = :id) AND " +
            "(:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
            "OR LOWER(c.nomeFantasia) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:cpfOuCnpj IS NULL OR " +
            " REPLACE(REPLACE(REPLACE(c.cpf, '.', ''), '-', ''), '/', '') = REPLACE(REPLACE(REPLACE(:cpfOuCnpj, '.', ''), '-', ''), '/', '') " +
            " OR REPLACE(REPLACE(REPLACE(c.cnpj, '.', ''), '-', ''), '/', '') = REPLACE(REPLACE(REPLACE(:cpfOuCnpj, '.', ''), '-', ''), '/', '')) AND " +
            "(:atividade IS NULL OR c.atividade.id = :atividade) AND " +
            "(:estado IS NULL OR EXISTS (SELECT 1 FROM ClienteEndereco e WHERE e.cliente.id = c.id AND e.estado = :estado)) AND" +
            "(:tipoPessoa IS NULL OR c.tipoPessoa = :tipoPessoa) AND " +
            "(:ativo IS NULL OR c.ativo = :ativo)")
    Page<Cliente> buscarClientes(
            @Param("id") Integer id,
            @Param("nome") String nome,
            @Param("cpfOuCnpj") String cpfOuCnpj,
            @Param("atividade") Integer atividade,
            @Param("estado") String estado,
            @Param("tipoPessoa") TipoPessoa tipoPessoa,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );

    Optional<Cliente> findByIdAndAtivoTrue(Integer idCliente);
}

