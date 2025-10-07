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
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanoService {

    public static final String VIEW_ONLY_PLAN_IDENTIFIER = PlatformConstants.VIEW_ONLY_PLAN_NAME;

    private final PlanoRepository planoRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ServicoRepository servicoRepository;
    private final PlanoMapper planoMapper;
    private final AuditTrailService auditTrailService;

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

        List<String> quotaChanges = new ArrayList<>();
        registrarAlteracaoQuota(quotaChanges, "maxUsuarios", existente.getMaxUsuarios(), request.getMaxUsuarios());
        existente.setMaxUsuarios(request.getMaxUsuarios());
        registrarAlteracaoQuota(quotaChanges, "maxProdutos", existente.getMaxProdutos(), request.getMaxProdutos());
        existente.setMaxProdutos(request.getMaxProdutos());
        registrarAlteracaoQuota(quotaChanges, "maxClientes", existente.getMaxClientes(), request.getMaxClientes());
        existente.setMaxClientes(request.getMaxClientes());
        registrarAlteracaoQuota(quotaChanges, "maxFornecedores", existente.getMaxFornecedores(), request.getMaxFornecedores());
        existente.setMaxFornecedores(request.getMaxFornecedores());
        registrarAlteracaoQuota(quotaChanges, "maxServicos", existente.getMaxServicos(), request.getMaxServicos());
        existente.setMaxServicos(request.getMaxServicos());

        registrarAlteracaoQuota(quotaChanges, "qtdAcessos", existente.getQtdAcessos(), request.getQtdAcessos());
        existente.setQtdAcessos(request.getQtdAcessos());

        registrarAlteracaoQuota(quotaChanges, "agendaHabilitada", existente.getAgendaHabilitada(), request.getAgendaHabilitada());
        existente.setAgendaHabilitada(request.getAgendaHabilitada());
        registrarAlteracaoQuota(quotaChanges, "recomendacoesHabilitadas", existente.getRecomendacoesHabilitadas(), request.getRecomendacoesHabilitadas());
        existente.setRecomendacoesHabilitadas(request.getRecomendacoesHabilitadas());
        registrarAlteracaoQuota(quotaChanges, "pagamentosHabilitados", existente.getPagamentosHabilitados(), request.getPagamentosHabilitados());
        existente.setPagamentosHabilitados(request.getPagamentosHabilitados());
        registrarAlteracaoQuota(quotaChanges, "suportePrioritario", existente.getSuportePrioritario(), request.getSuportePrioritario());
        existente.setSuportePrioritario(request.getSuportePrioritario());
        registrarAlteracaoQuota(quotaChanges, "monitoramentoEstoqueHabilitado", existente.getMonitoramentoEstoqueHabilitado(), request.getMonitoramentoEstoqueHabilitado());
        existente.setMonitoramentoEstoqueHabilitado(request.getMonitoramentoEstoqueHabilitado());
        registrarAlteracaoQuota(quotaChanges, "metricasProdutoHabilitadas", existente.getMetricasProdutoHabilitadas(), request.getMetricasProdutoHabilitadas());
        existente.setMetricasProdutoHabilitadas(request.getMetricasProdutoHabilitadas());
        registrarAlteracaoQuota(quotaChanges, "integracaoMarketplaceHabilitada", existente.getIntegracaoMarketplaceHabilitada(), request.getIntegracaoMarketplaceHabilitada());
        existente.setIntegracaoMarketplaceHabilitada(request.getIntegracaoMarketplaceHabilitada());

        if (!Objects.equals(existente.getNome(), request.getNome())) {
            existente.setNome(request.getNome());
        }
        if (!Objects.equals(existente.getValor(), request.getValor())) {
            existente.setValor(request.getValor());
        }
        if (!Objects.equals(existente.getDuracaoDias(), request.getDuracaoDias())) {
            existente.setDuracaoDias(request.getDuracaoDias());
        }

        Plano atualizado = planoRepository.save(existente);

        if (!quotaChanges.isEmpty()) {
            auditTrailService.recordPlanQuotaChange(atualizado.getId(), String.join("; ", quotaChanges));
        }

        return planoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "planos", allEntries = true)
    public void removerPlano(Integer idPlano) {
        Plano plano = planoRepository.findById(idPlano)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        planoRepository.delete(plano);
    }

    @Cacheable(
            value = "planos",
            key = "T(com.AIT.Optimanage.Services.PlanoService).resolveOrganizationId(#user)",
            condition = "T(com.AIT.Optimanage.Services.PlanoService).resolveOrganizationId(#user) != null"
    )
    public Optional<Plano> obterPlanoUsuario(User user) {
        Integer organizationId = resolveOrganizationId(user);
        if (organizationId == null) {
            return Optional.empty();
        }
        Integer finalOrganizationId = organizationId;
        return organizationRepository.findById(finalOrganizationId)
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById);
    }

    public boolean isPlanoSomenteVisualizacao(Plano plano) {
        if (plano == null) {
            return false;
        }
        String nome = plano.getNome();
        return nome != null && VIEW_ONLY_PLAN_IDENTIFIER.equalsIgnoreCase(nome.trim());
    }

    public PlanoQuotaResponse obterPlanoAtual(User user) {
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }

        Integer organizationId = resolveOrganizationId(user);

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
                .monitoramentoEstoqueHabilitado(plano.getMonitoramentoEstoqueHabilitado())
                .metricasProdutoHabilitadas(plano.getMetricasProdutoHabilitadas())
                .integracaoMarketplaceHabilitada(plano.getIntegracaoMarketplaceHabilitada())
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

    public boolean isMonitoramentoEstoqueHabilitado(Integer organizationId) {
        if (organizationId == null) {
            return false;
        }
        return organizationRepository.findById(organizationId)
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById)
                .map(Plano::getMonitoramentoEstoqueHabilitado)
                .orElse(false);
    }

    public boolean isMetricasProdutoHabilitadas(Integer organizationId) {
        if (organizationId == null) {
            return false;
        }
        return organizationRepository.findById(organizationId)
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById)
                .map(Plano::getMetricasProdutoHabilitadas)
                .orElse(false);
    }

    public boolean isIntegracaoMarketplaceHabilitada(Integer organizationId) {
        if (organizationId == null) {
            return false;
        }
        return organizationRepository.findById(organizationId)
                .map(Organization::getPlanoAtivoId)
                .flatMap(planoRepository::findById)
                .map(Plano::getIntegracaoMarketplaceHabilitada)
                .orElse(false);
    }

    private Integer calcularRestante(Integer maximo, long utilizado) {
        if (maximo == null || maximo <= 0) {
            return null;
        }
        long restante = maximo - utilizado;
        return (int) Math.max(0, restante);
    }

    public static Integer resolveOrganizationId(User user) {
        Integer organizationId = null;
        if (user != null) {
            organizationId = user.getTenantId();
        }
        if (organizationId == null) {
            organizationId = TenantContext.getTenantId();
        }
        return organizationId;
    }

    private void registrarAlteracaoQuota(List<String> quotaChanges, String campo, Object valorAnterior, Object valorAtual) {
        if (!Objects.equals(valorAnterior, valorAtual)) {
            quotaChanges.add(String.format("%s: %s -> %s", campo, valorAnterior, valorAtual));
        }
    }
}

