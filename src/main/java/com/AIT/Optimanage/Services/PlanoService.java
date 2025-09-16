package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.PlanoQuotaResponse;
import com.AIT.Optimanage.Controllers.dto.PlanoRequest;
import com.AIT.Optimanage.Controllers.dto.PlanoResponse;
import com.AIT.Optimanage.Mappers.PlanoMapper;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ServicoRepository servicoRepository;
    private final PlanoMapper planoMapper;

    public List<PlanoResponse> listarPlanos() {
        return planoRepository.findAll()
                .stream()
                .map(planoMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanoResponse criarPlano(PlanoRequest request) {
        Plano plano = planoMapper.toEntity(request);
        plano.setId(null);
        Plano salvo = planoRepository.save(plano);
        return planoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "planos", allEntries = true)
    public PlanoResponse atualizarPlano(Integer idPlano, PlanoRequest request) {
        Plano existente = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        Plano plano = planoMapper.toEntity(request);
        plano.setId(existente.getId());
        Plano atualizado = planoRepository.save(plano);
        return planoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "planos", allEntries = true)
    public void removerPlano(Integer idPlano) {
        Plano plano = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        planoRepository.delete(plano);
    }

    @Cacheable(value = "planos", key = "#user.id")
    public Optional<Plano> obterPlanoUsuario(User user) {
        Integer organizationId = user != null ? user.getTenantId() : null;
        if (organizationId == null) {
            organizationId = TenantContext.getTenantId();
        }
        if (organizationId == null) {
            return Optional.empty();
        }
        Integer finalOrganizationId = organizationId;
        return organizationRepository.findById(finalOrganizationId)
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById);
    }

    public PlanoQuotaResponse obterPlanoAtual(User user) {
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }

        Integer organizationId = user.getTenantId();
        if (organizationId == null) {
            organizationId = TenantContext.getTenantId();
        }

        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada para o usuário");
        }

        Plano plano = obterPlanoUsuario(user)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));

        long usuariosAtivos = userRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        long produtosAtivos = produtoRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        long clientesAtivos = clienteRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        long fornecedoresAtivos = fornecedorRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        long servicosAtivos = servicoRepository.countByOrganizationIdAndAtivoTrue(organizationId);

        return PlanoQuotaResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .valor(plano.getValor())
                .duracaoDias(plano.getDuracaoDias())
                .qtdAcessos(plano.getQtdAcessos())
                .maxUsuarios(plano.getMaxUsuarios())
                .maxProdutos(plano.getMaxProdutos())
                .maxClientes(plano.getMaxClientes())
                .maxFornecedores(plano.getMaxFornecedores())
                .maxServicos(plano.getMaxServicos())
                .agendaHabilitada(plano.getAgendaHabilitada())
                .recomendacoesHabilitadas(plano.getRecomendacoesHabilitadas())
                .pagamentosHabilitados(plano.getPagamentosHabilitados())
                .suportePrioritario(plano.getSuportePrioritario())
                .usuariosUtilizados(Math.toIntExact(usuariosAtivos))
                .usuariosRestantes(calcularRestante(plano.getMaxUsuarios(), usuariosAtivos))
                .produtosUtilizados(Math.toIntExact(produtosAtivos))
                .produtosRestantes(calcularRestante(plano.getMaxProdutos(), produtosAtivos))
                .clientesUtilizados(Math.toIntExact(clientesAtivos))
                .clientesRestantes(calcularRestante(plano.getMaxClientes(), clientesAtivos))
                .fornecedoresUtilizados(Math.toIntExact(fornecedoresAtivos))
                .fornecedoresRestantes(calcularRestante(plano.getMaxFornecedores(), fornecedoresAtivos))
                .servicosUtilizados(Math.toIntExact(servicosAtivos))
                .servicosRestantes(calcularRestante(plano.getMaxServicos(), servicosAtivos))
                .build();
    }

    private Integer calcularRestante(Integer maximo, long utilizado) {
        if (maximo == null || maximo <= 0) {
            return null;
        }
        long restante = maximo - utilizado;
        return (int) Math.max(0, restante);
    }
}

