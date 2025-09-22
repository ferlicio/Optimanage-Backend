package com.AIT.Optimanage.Repositories.Venda;

import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.AtividadeAplicavelA;
import com.AIT.Optimanage.Models.Enums.TipoAtividade;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VendaRepositoryTest {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Venda vendaOrganizacaoUmSeqUm;
    private Venda vendaOrganizacaoUmSeqDois;

    @BeforeEach
    void setUp() {
        Cliente clienteOrganizacaoUm = createCliente(1, "Cliente Org1");
        vendaOrganizacaoUmSeqUm = createVenda(clienteOrganizacaoUm, 1, BigDecimal.valueOf(100));
        vendaOrganizacaoUmSeqDois = createVenda(clienteOrganizacaoUm, 2, BigDecimal.valueOf(200));

        Cliente clienteOrganizacaoDois = createCliente(2, "Cliente Org2");
        createVenda(clienteOrganizacaoDois, 3, BigDecimal.valueOf(300));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve filtrar por sequencialUsuario quando o id é informado")
    void deveFiltrarPorSequencialQuandoIdInformado() {
        Page<Venda> resultado = vendaRepository.buscarVendas(
                vendaOrganizacaoUmSeqUm.getOrganizationId(),
                vendaOrganizacaoUmSeqUm.getSequencialUsuario(),
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(resultado.getContent())
                .extracting(Venda::getId)
                .containsExactly(vendaOrganizacaoUmSeqUm.getId());
    }

    @Test
    @DisplayName("Não deve aplicar filtro por sequencialUsuario quando id é nulo")
    void naoDeveFiltrarPorSequencialQuandoIdNulo() {
        Page<Venda> resultado = vendaRepository.buscarVendas(
                vendaOrganizacaoUmSeqUm.getOrganizationId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(resultado.getContent())
                .extracting(Venda::getId)
                .containsExactlyInAnyOrder(
                        vendaOrganizacaoUmSeqUm.getId(),
                        vendaOrganizacaoUmSeqDois.getId()
                );
    }

    private Cliente createCliente(int organizationId, String nome) {
        Atividade atividade = new Atividade();
        atividade.setNomeAtividade("Atividade " + nome);
        atividade.setTipo(TipoAtividade.PADRAO);
        atividade.setAplicavelA(AtividadeAplicavelA.AMBOS);
        atividade.setOrganizationId(organizationId);
        entityManager.persist(atividade);

        Cliente cliente = new Cliente();
        cliente.setAtividade(atividade);
        cliente.setDataCadastro(LocalDate.now());
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setOrigem("ORIGEM");
        cliente.setAtivo(true);
        cliente.setNome(nome);
        cliente.setOrganizationId(organizationId);
        entityManager.persist(cliente);

        return cliente;
    }

    private Venda createVenda(Cliente cliente, int sequencialUsuario, BigDecimal valorTotal) {
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setSequencialUsuario(sequencialUsuario);
        venda.setDataEfetuacao(LocalDate.now());
        venda.setDataCobranca(LocalDate.now());
        venda.setValorTotal(valorTotal);
        venda.setDescontoGeral(BigDecimal.ZERO);
        venda.setValorFinal(valorTotal);
        venda.setValorPendente(BigDecimal.ZERO);
        venda.setStatus(StatusVenda.PENDENTE);
        venda.setOrganizationId(cliente.getOrganizationId());
        entityManager.persist(venda);
        return venda;
    }
}
