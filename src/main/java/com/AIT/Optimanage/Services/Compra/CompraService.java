package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Events.CompraCriadaEvent;
import com.AIT.Optimanage.Events.InventoryAdjustment;
import com.AIT.Optimanage.Mappers.CompraMapper;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import com.AIT.Optimanage.Models.Compra.CompraServico;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraProdutoDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraResponseDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraServicoDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Compra.Search.CompraSearch;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Compra.CompraFilters;
import com.AIT.Optimanage.Repositories.Compra.CompraProdutoRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraServicoRepository;
import com.AIT.Optimanage.Repositories.FilterBuilder;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.InventoryService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Services.common.StatusTransitionPolicy;
import com.AIT.Optimanage.Services.common.StatusTransitionPolicies;
import com.AIT.Optimanage.Validation.AgendaValidator;
import com.AIT.Optimanage.Validation.CompraValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import com.AIT.Optimanage.Repositories.Compra.CompraFilters;
import com.AIT.Optimanage.Repositories.FilterBuilder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private static final StatusTransitionPolicy<StatusCompra, Compra> STATUS_TRANSITION_POLICY =
            StatusTransitionPolicies.compraPolicy();

    private final CompraRepository compraRepository;
    private final FornecedorService fornecedorService;
    private final ContadorService contadorService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final PlanoService planoService;
    private final CompraProdutoRepository compraProdutoRepository;
    private final CompraServicoRepository compraServicoRepository;
    private final PagamentoCompraService pagamentoCompraService;
    private final CompraMapper compraMapper;
    private final CompraValidator compraValidator;
    private final AgendaValidator agendaValidator;
    private final OrganizationRepository organizationRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Cacheable(value = "compras", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId() + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<CompraResponseDTO> listarCompras(CompraSearch pesquisa) {
        User loggedUser = CurrentUser.get();
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        Specification<Compra> spec = FilterBuilder
                .of(CompraFilters.hasOrganization(organizationId))
                .and(pesquisa.getId(), CompraFilters::hasSequencialUsuario)
                .and(pesquisa.getFornecedorId(), CompraFilters::hasFornecedor)
                .and(pesquisa.getDataInicial(), d -> CompraFilters.dataEfetuacaoAfter(LocalDate.parse(d)))
                .and(pesquisa.getDataFinal(), d -> CompraFilters.dataEfetuacaoBefore(LocalDate.parse(d)))
                .and(pesquisa.getStatus(), CompraFilters::hasStatus)
                .and(pesquisa.getPago(), CompraFilters::isPago)
                .and(pesquisa.getFormaPagamento(), CompraFilters::hasFormaPagamento)
                .build();

        Page<Compra> compras = compraRepository.findAll(spec, pageable);
        return compras.map(compraMapper::toResponse);
    }

    private Compra getCompra(Integer idCompra) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return compraRepository.findByIdAndOrganizationId(idCompra, organizationId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
    }

    public CompraResponseDTO listarUmaCompra(Integer idCompra) {
        return compraMapper.toResponse(getCompra(idCompra));
    }

    @Transactional
    @CacheEvict(value = "compras", allEntries = true)
    public CompraResponseDTO criarCompra(CompraDTO compraDTO) {
        compraValidator.validarCompra(compraDTO);
        if (compraDTO.getDataAgendada() != null) {
            Plano plano = obterPlanoAtual();
            garantirAgendaHabilitada(plano);
        }

        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(compraDTO.getFornecedorId());
        Contador contador = contadorService.BuscarContador(Tabela.COMPRA);
        Compra novaCompra = Compra.builder()
                .fornecedor(fornecedor)
                .sequencialUsuario(contador.getContagemAtual())
                .dataEfetuacao(compraDTO.getDataEfetuacao())
                .dataAgendada(compraDTO.getDataAgendada())
                .horaAgendada(compraDTO.getHoraAgendada())
                .duracaoEstimada(compraDTO.getDuracaoEstimada())
                .valorFinal(BigDecimal.ZERO)
                .condicaoPagamento(compraDTO.getCondicaoPagamento())
                .valorPendente(BigDecimal.ZERO)
                .status(compraDTO.getStatus())
                .observacoes(compraDTO.getObservacoes())
                .build();

        List<CompraProduto> compraProdutos = criarListaProdutos(compraDTO.getProdutos(), novaCompra);
        List<CompraServico> compraServicos = criarListaServicos(compraDTO.getServicos(), novaCompra);
        novaCompra.setCompraProdutos(compraProdutos);
        novaCompra.setCompraServicos(compraServicos);

        BigDecimal valorProdutos = compraProdutos.stream()
                .map(CompraProduto::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorServicos = compraServicos.stream()
                .map(CompraServico::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTotal = valorProdutos.add(valorServicos);
        BigDecimal valorPago = BigDecimal.ZERO;

        novaCompra.setValorFinal(valorTotal);
        novaCompra.setValorPendente(valorTotal.subtract(valorPago));

        
        novaCompra.setTenantId(organizationId);
        compraRepository.save(novaCompra);
        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);

        publicarCompraCriada(novaCompra, compraProdutos);

        contadorService.IncrementarContador(Tabela.COMPRA);
        return compraMapper.toResponse(novaCompra);
    }

    @Transactional
    @CacheEvict(value = "compras", allEntries = true)
    public CompraResponseDTO editarCompra(Integer idCompra, CompraDTO compraDTO) {
        compraValidator.validarCompra(compraDTO);
        if (compraDTO.getDataAgendada() != null) {
            Plano plano = obterPlanoAtual();
            garantirAgendaHabilitada(plano);
        }

        Compra compra = getCompra(idCompra);
        StatusCompra statusAnterior = compra.getStatus();

        compra.setDataEfetuacao(compraDTO.getDataEfetuacao());
        compra.setDataAgendada(compraDTO.getDataAgendada());
        compra.setHoraAgendada(compraDTO.getHoraAgendada());
        compra.setDuracaoEstimada(compraDTO.getDuracaoEstimada());
        compra.setCondicaoPagamento(compraDTO.getCondicaoPagamento());
        compra.setObservacoes(compraDTO.getObservacoes());

        if (statusAnterior != StatusCompra.ORCAMENTO) {
            reverterEstoqueCompra(compra, "Ajuste de edição da compra #" + compra.getId());
        }

        compraProdutoRepository.deleteAll(compra.getCompraProdutos());
        compraServicoRepository.deleteAll(compra.getCompraServicos());

        List<CompraProduto> compraProdutos = criarListaProdutos(compraDTO.getProdutos(), compra);
        List<CompraServico> compraServicos = criarListaServicos(compraDTO.getServicos(), compra);
        compra.setCompraProdutos(compraProdutos);
        compra.setCompraServicos(compraServicos);

        BigDecimal valorProdutos = compraProdutos.stream()
                .map(CompraProduto::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorServicos = compraServicos.stream()
                .map(CompraServico::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTotal = valorProdutos.add(valorServicos);

        BigDecimal valorPago = compra.getPagamentos() == null
                ? BigDecimal.ZERO
                : compra.getPagamentos().stream().map(CompraPagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);

        compra.setValorFinal(valorTotal);
        compra.setValorPendente(valorTotal.subtract(valorPago));

        if (compraDTO.getStatus() != null && compraDTO.getStatus() != compra.getStatus()) {
            atualizarStatus(compra, compraDTO.getStatus());
        }

        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);

        if (compra.getStatus() != StatusCompra.ORCAMENTO) {
            compraProdutos.forEach(cp ->
                    inventoryService.incrementar(cp.getProduto().getId(), cp.getQuantidade(), InventorySource.COMPRA,
                            compra.getId(), "Atualização da compra #" + compra.getId()));
        }

        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    @Transactional
    public CompraResponseDTO confirmarCompra(Integer idCompra) {
        Compra compra = getCompra(idCompra);
        StatusCompra statusAnterior = compra.getStatus();
        if (statusAnterior == StatusCompra.ORCAMENTO && compra.getCompraServicos().isEmpty()) {
            atualizarStatus(compra, StatusCompra.AGUARDANDO_PAG);
        } else {
            atualizarStatus(compra, StatusCompra.AGUARDANDO_EXECUCAO);
        }
        boolean devePublicarEvento = statusAnterior == StatusCompra.ORCAMENTO;
        Compra salvo = compraRepository.save(compra);
        if (devePublicarEvento) {
            publicarCompraCriada(salvo, salvo.getCompraProdutos());
        }
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO pagarCompra(Integer idCompra, Integer idPagamento) {
        Plano plano = obterPlanoAtual();
        garantirPagamentosHabilitados(plano);
        Compra compra = getCompra(idCompra);
        podePagarCompra(compra);

        pagamentoCompraService.registrarPagamento(compra, idPagamento);

        atualizarCompraPosPagamento(compra);
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    @Transactional
    public CompraResponseDTO lancarPagamentoCompra(Integer idCompra, List<PagamentoDTO> pagamentoDTO) {
        Plano plano = obterPlanoAtual();
        garantirPagamentosHabilitados(plano);
        Compra compra = getCompra(idCompra);
        podePagarCompra(compra);

        for (PagamentoDTO pagamento : pagamentoDTO) {
            if (pagamento.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor de pagamento deve ser maior que zero");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PAGO &&
                    pagamento.getDataPagamento() != null && pagamento.getDataPagamento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Um pagamento realizado não pode ser no futuro");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PENDENTE &&
                    pagamento.getDataVencimento().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Um pagamento pendente não pode ter vencimento no passado");
            }
            pagamentoCompraService.lancarPagamento(compra, pagamento);
        }

        atualizarCompraPosPagamento(compra);
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO estornarCompraIntegral(Integer idCompra) {
        Plano plano = obterPlanoAtual();
        garantirPagamentosHabilitados(plano);
        Compra compra = getCompra(idCompra);
        boolean deveReverterEstoque = compra.getStatus() == StatusCompra.CONCRETIZADO
                || compra.getStatus() == StatusCompra.PAGO;
        if (deveReverterEstoque) {
            reverterEstoqueCompra(compra, "Estorno integral da compra #" + compra.getId());
            atualizarStatus(compra, StatusCompra.AGUARDANDO_PAG);
        }
        compra.setValorPendente(compra.getValorFinal());
        compra.getPagamentos().forEach(pagamento
                -> { if (pagamento.getStatusPagamento() == StatusPagamento.PAGO)
                        { pagamentoCompraService.estornarPagamento(pagamento); }
                    });
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO estornarPagamentoCompra(Integer idCompra, Integer idPagamento) {
        Plano plano = obterPlanoAtual();
        garantirPagamentosHabilitados(plano);
        Compra compra = getCompra(idCompra);
        CompraPagamento pagamento = pagamentoCompraService.listarUmPagamento(idPagamento);
        if (compra.getPagamentos().contains(pagamento) && pagamento.getStatusPagamento() == StatusPagamento.PAGO) {
            pagamentoCompraService.estornarPagamento(pagamento);
        } else {
            throw new IllegalArgumentException("O pagamento informado não pode ser estornado");
        }
        BigDecimal valorPago = compra.getPagamentos().stream()
                .filter(pagamentoCompra -> pagamentoCompra.getStatusPagamento() == StatusPagamento.PAGO)
                .map(CompraPagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setValorPendente(compra.getValorFinal().subtract(valorPago));
        if (compra.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            if (compra.getStatus() == StatusCompra.AGUARDANDO_PAG || compra.getStatus() == StatusCompra.PARCIALMENTE_PAGO) {
                atualizarStatus(compra, StatusCompra.PAGO);
            }
        } else if (valorPago.compareTo(BigDecimal.ZERO) > 0) {
            atualizarStatus(compra, StatusCompra.PARCIALMENTE_PAGO);
        }
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO agendarCompra(Integer idCompra, String dataAgendada, String horaAgendada,
                                           Integer duracaoMinutos) {
        Plano plano = obterPlanoAtual();
        garantirAgendaHabilitada(plano);
        Compra compra = getCompra(idCompra);

        boolean semItens = (compra.getCompraProdutos() == null || compra.getCompraProdutos().isEmpty())
                && (compra.getCompraServicos() == null || compra.getCompraServicos().isEmpty());
        if (semItens) {
            throw new IllegalArgumentException("Não é possível agendar uma compra sem produtos ou serviços.");
        }

        if (compra.getStatus() != StatusCompra.AGUARDANDO_EXECUCAO && compra.getStatus() != StatusCompra.PAGO) {
            throw new IllegalStateException(
                    "Não é possível agendar uma compra com status " + compra.getStatus() + ".");
        }

        LocalDate data = agendaValidator.validarDataAgendamento(dataAgendada);
        LocalTime hora = agendaValidator.validarHoraAgendada(horaAgendada);
        Duration duracao = agendaValidator.validarDuracao(duracaoMinutos);

        Integer userId = Optional.ofNullable(compra.getCreatedBy())
                .orElseGet(() -> Optional.ofNullable(CurrentUser.get()).map(User::getId).orElse(null));
        agendaValidator.validarConflitosAgendamentoCompra(compra, userId, data, hora, duracao);

        compra.setDataAgendada(data);
        compra.setHoraAgendada(hora);
        compra.setDuracaoEstimada(duracao);
        atualizarStatus(compra, StatusCompra.AGENDADA);

        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO finalizarAgendamentoCompra(Integer idCompra) {
        Plano plano = obterPlanoAtual();
        garantirAgendaHabilitada(plano);
        Compra compra = getCompra(idCompra);
        if (compra.getStatus() == StatusCompra.AGENDADA) {
            if (compra.getValorPendente().compareTo(BigDecimal.ZERO) > 0) {
                compra.setStatus(StatusCompra.AGUARDANDO_PAG);
            } else {
                compra.setStatus(StatusCompra.CONCRETIZADO);
            }
        } else {
            throw new IllegalArgumentException("Não é possível finalizar um agendamento que não está agendado.");
        }
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    public CompraResponseDTO finalizarCompra(Integer idCompra) {
        Compra compra = getCompra(idCompra);
        atualizarStatus(compra, StatusCompra.CONCRETIZADO);
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    @CacheEvict(value = "compras", allEntries = true)
    public CompraResponseDTO cancelarCompra(Integer idCompra) {
        Compra compra = getCompra(idCompra);
        StatusCompra statusAnterior = compra.getStatus();
        STATUS_TRANSITION_POLICY.validate(statusAnterior, StatusCompra.CANCELADO, compra);

        if (deveReverterEstoqueAoCancelar(compra, statusAnterior)) {
            reverterEstoqueCompra(compra, "Cancelamento da compra #" + compra.getId());
        }

        compra.setStatus(StatusCompra.CANCELADO);
        Compra salvo = compraRepository.save(compra);
        return compraMapper.toResponse(salvo);
    }

    private void reverterEstoqueCompra(Compra compra, String descricao) {
        if (compra == null) {
            return;
        }

        List<CompraProduto> itens = compra.getCompraProdutos();
        if (itens == null || itens.isEmpty()) {
            return;
        }

        itens.stream()
                .filter(item -> item != null && item.getProduto() != null)
                .forEach(item -> inventoryService.reduzir(
                        item.getProduto().getId(),
                        item.getQuantidade(),
                        InventorySource.COMPRA,
                        compra.getId(),
                        descricao));
    }

    private List<CompraProduto> criarListaProdutos(List<CompraProdutoDTO> produtosDTO, Compra compra) {
        return produtosDTO.stream()
                .map(produtoDTO -> {
                    Produto produto = produtoService.buscarProdutoAtivo(produtoDTO.getProdutoId());
                    BigDecimal valorUnitario = Optional.ofNullable(produtoDTO.getValorUnitario())
                            .orElse(produto.getCusto());
                    BigDecimal valorTotal = valorUnitario
                            .multiply(BigDecimal.valueOf(produtoDTO.getQuantidade()));

                    return CompraProduto.builder()
                            .compra(compra)
                            .produto(produto)
                            .valorUnitario(valorUnitario)
                            .quantidade(produtoDTO.getQuantidade())
                            .valorTotal(valorTotal)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private boolean deveReverterEstoqueAoCancelar(Compra compra, StatusCompra statusAnterior) {
        if (compra == null || compra.getCompraProdutos() == null || compra.getCompraProdutos().isEmpty()) {
            return false;
        }

        if (statusAnterior == StatusCompra.CONCRETIZADO || statusAnterior == StatusCompra.ORCAMENTO) {
            return false;
        }

        return !estoqueRevertidoPorEstorno(compra);
    }

    private boolean estoqueRevertidoPorEstorno(Compra compra) {
        List<CompraPagamento> pagamentos = compra.getPagamentos();
        if (pagamentos == null || pagamentos.isEmpty()) {
            return false;
        }

        boolean possuiEstornado = pagamentos.stream()
                .filter(Objects::nonNull)
                .anyMatch(pagamento -> pagamento.getStatusPagamento() == StatusPagamento.ESTORNADO);

        if (!possuiEstornado) {
            return false;
        }

        return pagamentos.stream()
                .filter(Objects::nonNull)
                .noneMatch(pagamento -> pagamento.getStatusPagamento() == StatusPagamento.PAGO);
    }

    private void publicarCompraCriada(Compra compra, List<CompraProduto> itens) {
        if (compra.getStatus() == StatusCompra.ORCAMENTO || itens == null || itens.isEmpty()) {
            return;
        }

        Integer organizationId = Optional.ofNullable(compra.getOrganizationId())
                .orElse(CurrentUser.getOrganizationId());

        List<InventoryAdjustment> ajustes = itens.stream()
                .map(item -> new InventoryAdjustment(item.getProduto().getId(), item.getQuantidade()))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(new CompraCriadaEvent(compra.getId(), organizationId, ajustes));
    }

    private List<CompraServico> criarListaServicos(List<CompraServicoDTO> servicosDTO, Compra compra) {
        return servicosDTO.stream()
                .map(servicoDTO -> {
                    Servico servico = servicoService.buscarServicoAtivo(servicoDTO.getServicoId());
                    BigDecimal valorUnitario = Optional.ofNullable(servico.getCusto())
                            .orElse(servico.getValorVenda());
                    BigDecimal valorTotal = valorUnitario.multiply(BigDecimal.valueOf(servicoDTO.getQuantidade()));

                    return CompraServico.builder()
                            .compra(compra)
                            .servico(servico)
                            .valorUnitario(valorUnitario)
                            .quantidade(servicoDTO.getQuantidade())
                            .valorTotal(valorTotal)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void atualizarStatus(Compra compra, StatusCompra novoStatus) {
        STATUS_TRANSITION_POLICY.validate(compra.getStatus(), novoStatus, compra);
        compra.setStatus(novoStatus);
    }

    public void podePagarCompra(Compra compra) {
        if (compra.getStatus() == StatusCompra.CONCRETIZADO) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para uma compra já concretizada");
        } else if (compra.getStatus() == StatusCompra.CANCELADO) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para uma compra cancelada");
        } else if (compra.getStatus() == StatusCompra.PAGO || compra.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para uma compra já paga");
        } else if (compra.getStatus() == StatusCompra.ORCAMENTO) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para um orçamento");
        }
    }

    public void atualizarCompraPosPagamento(Compra compra) {
        List<CompraPagamento> pagamentos = pagamentoCompraService.listarPagamentosRealizadosCompra(compra.getId());

        BigDecimal valorPago = pagamentos.stream().map(CompraPagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPendente = compra.getValorFinal().subtract(valorPago);
        if (valorPendente.compareTo(BigDecimal.ZERO) < 0) {
            valorPendente = BigDecimal.ZERO;
        }
        compra.setValorPendente(valorPendente);

        if (compra.getValorPendente().compareTo(BigDecimal.ZERO) == 0) {
            if (compra.getStatus() == StatusCompra.AGUARDANDO_PAG || compra.getStatus() == StatusCompra.PARCIALMENTE_PAGO) {
                atualizarStatus(compra, StatusCompra.PAGO);
            }
        } else {
            if (compra.getStatus() == StatusCompra.AGUARDANDO_PAG) {
                atualizarStatus(compra, StatusCompra.PARCIALMENTE_PAGO);
            }
        }
    }

    private Plano obterPlanoAtual() {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        return planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
    }

    private void garantirPagamentosHabilitados(Plano plano) {
        if (!Boolean.TRUE.equals(plano.getPagamentosHabilitados())) {
            throw new AccessDeniedException("Pagamentos não estão habilitados no plano atual");
        }
    }

    private void garantirAgendaHabilitada(Plano plano) {
        if (!Boolean.TRUE.equals(plano.getAgendaHabilitada())) {
            throw new AccessDeniedException("Agenda não está habilitada no plano atual");
        }
    }
}
