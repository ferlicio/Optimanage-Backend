package com.AIT.Optimanage.Repositories.Fornecedor;

import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Enums.AtividadeAplicavelA;
import com.AIT.Optimanage.Models.Enums.TipoAtividade;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@ActiveProfiles("test")
class FornecedorRepositoryTest {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void buscarFornecedoresFiltraPorEstado() {
        Atividade atividade = novaAtividade();

        criarFornecedor("Fornecedor SP", atividade, "SP");
        criarFornecedor("Fornecedor RJ", atividade, "RJ");
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));

        Page<Fornecedor> resultado = fornecedorRepository.buscarFornecedores(
                1,
                null,
                null,
                null,
                null,
                "SP",
                null,
                null,
                pageable
        );

        assertThat(resultado.getContent())
                .extracting(Fornecedor::getNome)
                .containsExactly("Fornecedor SP");
    }

    private Atividade novaAtividade() {
        Atividade atividade = Atividade.builder()
                .nomeAtividade("Comércio")
                .tipo(TipoAtividade.PADRAO)
                .aplicavelA(AtividadeAplicavelA.AMBOS)
                .build();
        return entityManager.persist(atividade);
    }

    private Fornecedor criarFornecedor(String nome, Atividade atividade, String estado) {
        Fornecedor fornecedor = Fornecedor.builder()
                .atividade(atividade)
                .dataCadastro(LocalDate.now())
                .tipoPessoa(TipoPessoa.PJ)
                .origem("TESTE")
                .ativo(true)
                .nome(nome)
                .build();

        FornecedorEndereco endereco = FornecedorEndereco.builder()
                .fornecedor(fornecedor)
                .nomeUnidade("Matriz")
                .cep("01001000")
                .estado(estado)
                .cidade("Cidade")
                .bairro("Centro")
                .logradouro("Rua Um")
                .numero(100)
                .build();

        fornecedor.setEnderecos(List.of(endereco));

        return entityManager.persist(fornecedor);
    }
}

