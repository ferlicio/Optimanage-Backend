package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Events.InventoryAdjustment;
import com.AIT.Optimanage.Events.VendaRegistradaEvent;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Inventory.InventorySource;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaResponseDTO;
import com.AIT.Optimanage.Mappers.VendaMapper;
import com.AIT.Optimanage.Models.Venda.Search.VendaSearch;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Payments.PaymentConfirmationDTO;
import com.AIT.Optimanage.Payments.PaymentRequestDTO;
import com.AIT.Optimanage.Payments.PaymentResponseDTO;
import com.AIT.Optimanage.Payments.PaymentService;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Services.Payment.PaymentConfigService;
import com.AIT.Optimanage.Repositories.Venda.VendaProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaServicoRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Services.Cliente.ClienteService;
import com.AIT.Optimanage.Services.InventoryService;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import com.AIT.Optimanage.Services.common.StatusTransitionPolicy;
import com.AIT.Optimanage.Services.common.StatusTransitionPolicies;
import com.AIT.Optimanage.Validation.AgendaValidator;
import com.AIT.Optimanage.Validation.VendaValidator;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendaService {

    private static final StatusTransitionPolicy<StatusVenda, Venda> STATUS_TRANSITION_POLICY =
            StatusTransitionPolicies.vendaPolicy();

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final PlanoService planoService;
    private final VendaProdutoRepository vendaProdutoRepository;
    private final VendaServicoRepository vendaServicoRepository;
    private final ContadorService contadorService;
    private final PagamentoVendaService pagamentoVendaService;
    private final PaymentService paymentService;
    private final PaymentConfigService paymentConfigService;
    private final VendaMapper vendaMapper;
    private final VendaValidator vendaValidator;
    private final AgendaValidator agendaValidator;
    private final OrganizationRepository organizationRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Cacheable(value = "vendas", key = "T(com.AIT.Optimanage.Support.CacheKeyResolver).userScopedKey(#loggedUser, #pesquisa)")
    @Transactional(readOnly = true)
    public Page<VendaResponseDTO> listarVendas(User loggedUser, VendaSearch pesquisa) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuario logado
        return vendaRepository.buscarVendas(
                organizationId,
                pesquisa.getId(),
                pesquisa.getClienteId(),
                pesquisa.getDataInicial(),
                pesquisa.getDataFinal(),
                pesquisa.getStatus(),
                pesquisa.getPago(),
                pesquisa.getFormaPagamento(),
                pageable).map(vendaMapper::toResumo);
    }

    private Venda getVenda(User loggedUser, Integer idVenda) {
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return vendaRepository.findDetailedByIdAndOrganizationId(idVenda, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada"));
    }

    public VendaResponseDTO listarUmaVenda(User loggedUser, Integer idVenda) {
        return vendaMapper.toResponse(getVenda(loggedUser, idVenda));
    }

    @Transactional
    @CacheEvict(value = "vendas", allEntries = true)
    public VendaResponseDTO registrarVenda(User loggedUser, VendaDTO vendaDTO) {
        vendaValidator.validarVenda(vendaDTO, loggedUser);
        if (vendaDTO.getDataAgendada() != null) {
            Plano plano = obterPlanoAtivo(loggedUser);
            garantirAgendaHabilitada(plano);
        }

        Cliente cliente = clienteService.listarUmCliente(vendaDTO.getClienteId());
        Contador contador = contadorService.BuscarContador(Tabela.VENDA);
        Integer organizationId = loggedUser != null ? loggedUser.getTenantId() : CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        BigDecimal descontoGeral = Optional.ofNullable(vendaDTO.getDescontoGeral()).orElse(BigDecimal.ZERO);

        Venda novaVenda = Venda.builder()
                .cliente(cliente)
                .sequencialUsuario(contador.getContagemAtual())
                .dataEfetuacao(vendaDTO.getDataEfetuacao())
                .dataAgendada(vendaDTO.getDataAgendada())
                .horaAgendada(vendaDTO.getHoraAgendada())
                .duracaoEstimada(vendaDTO.getDuracaoEstimada())
                .dataCobranca(vendaDTO.getDataCobranca())
                .valorTotal(BigDecimal.ZERO)
                .descontoGeral(descontoGeral)
                .valorFinal(BigDecimal.ZERO)
                .condicaoPagamento(vendaDTO.getCondicaoPagamento())
                .alteracoesPermitidas(vendaDTO.getAlteracoesPermitidas())
                .valorPendente(BigDecimal.ZERO)
                .status(vendaDTO.getStatus())
                .observacoes(vendaDTO.getObservacoes())
                .build();

        List<VendaProduto> vendaProdutos = criarListaProdutos(vendaDTO.getProdutos(), novaVenda);
        List<VendaServico> vendaServicos = criarListaServicos(vendaDTO.getServicos(), novaVenda);

        BigDecimal valorProdutos = vendaProdutos.stream()
                .map(VendaProduto::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorServicos = vendaServicos.stream()
                .map(VendaServico::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTotal = valorProdutos.add(valorServicos).setScale(2, RoundingMode.HALF_UP);

        BigDecimal valorPago = BigDecimal.ZERO;

        novaVenda.setValorTotal(valorTotal);
        BigDecimal valorFinal = valorTotal.multiply(BigDecimal.valueOf(100).subtract(novaVenda.getDescontoGeral()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        novaVenda.setValorFinal(valorFinal);
        novaVenda.setValorPendente(valorFinal.subtract(valorPago).setScale(2, RoundingMode.HALF_UP));
        novaVenda.setVendaProdutos(vendaProdutos);
        novaVenda.setVendaServicos(vendaServicos);
        novaVenda.setTenantId(organizationId);

        vendaRepository.save(novaVenda);
        vendaProdutoRepository.saveAll(vendaProdutos);
        vendaServicoRepository.saveAll(vendaServicos);

        publicarVendaRegistrada(novaVenda, vendaProdutos, loggedUser);

        contadorService.IncrementarContador(Tabela.VENDA);
        return vendaMapper.toResponse(novaVenda);
    }

    @Transactional
    @CacheEvict(value = "vendas", allEntries = true)
    public VendaResponseDTO atualizarVenda(User loggedUser, Integer vendaId, VendaDTO vendaDTO) {
        vendaValidator.validarVenda(vendaDTO, loggedUser);
        if (vendaDTO.getDataAgendada() != null) {
            Plano plano = obterPlanoAtivo(loggedUser);
            garantirAgendaHabilitada(plano);
        }

        Venda venda = getVenda(loggedUser, vendaId);
        BigDecimal descontoGeralAtualizado = Optional.ofNullable(vendaDTO.getDescontoGeral()).orElse(BigDecimal.ZERO);

        venda.setDataEfetuacao(vendaDTO.getDataEfetuacao());
        venda.setDataAgendada(vendaDTO.getDataAgendada());
        venda.setHoraAgendada(vendaDTO.getHoraAgendada());
        venda.setDuracaoEstimada(vendaDTO.getDuracaoEstimada());
        venda.setDataCobranca(vendaDTO.getDataCobranca());
        venda.setCondicaoPagamento(vendaDTO.getCondicaoPagamento());
        venda.setAlteracoesPermitidas(Optional.ofNullable(vendaDTO.getAlteracoesPermitidas()).orElse(0));
        venda.setObservacoes(vendaDTO.getObservacoes());
        venda.setDescontoGeral(descontoGeralAtualizado);

        StatusVenda statusAnterior = venda.getStatus();

        // Devolve estoque dos produtos antigos e remove os registros
        if (statusAnterior != StatusVenda.ORCAMENTO) {
            devolverProdutosParaEstoque(venda, "Reversão da venda #" + venda.getId());
        }
        vendaProdutoRepository.deleteByVenda(venda);
        vendaServicoRepository.deleteByVenda(venda);

        List<VendaProduto> vendaProdutos = criarListaProdutos(vendaDTO.getProdutos(), venda);
        List<VendaServico> vendaServicos = criarListaServicos(vendaDTO.getServicos(), venda);

        BigDecimal valorProdutos = vendaProdutos.stream()
                .map(VendaProduto::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorServicos = vendaServicos.stream()
                .map(VendaServico::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTotal = valorProdutos.add(valorServicos).setScale(2, RoundingMode.HALF_UP);

        BigDecimal valorPago = venda.getPagamentos() == null
                ? BigDecimal.ZERO
                : venda.getPagamentos().stream().map(VendaPagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);

        venda.setValorTotal(valorTotal);
        BigDecimal valorFinalAtualizado = valorTotal.multiply(BigDecimal.valueOf(100).subtract(descontoGeralAtualizado))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        venda.setValorFinal(valorFinalAtualizado);
        BigDecimal valorPendenteAtualizado = valorFinalAtualizado.subtract(valorPago).setScale(2, RoundingMode.HALF_UP);
        if (valorPendenteAtualizado.compareTo(BigDecimal.ZERO) < 0) {
            valorPendenteAtualizado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        venda.setValorPendente(valorPendenteAtualizado);
        venda.setVendaProdutos(vendaProdutos);
        venda.setVendaServicos(vendaServicos);
        atualizarStatus(venda, vendaDTO.getStatus());

        vendaProdutoRepository.saveAll(vendaProdutos);
        vendaServicoRepository.saveAll(vendaServicos);

        if (venda.getStatus() != StatusVenda.ORCAMENTO) {
            vendaProdutos.forEach(vp ->
                    inventoryService.reduzir(vp.getProduto().getId(), vp.getQuantidade(), InventorySource.VENDA,
                            venda.getId(), "Atualização da venda #" + venda.getId()));
        }

        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO confirmarVenda(User loggedUser, Integer idVenda) {
        Venda venda = getVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.ORCAMENTO && venda.getVendaServicos().isEmpty()) {
            atualizarStatus(venda, StatusVenda.PENDENTE);
        } else if (venda.getStatus() == StatusVenda.ORCAMENTO) {
            atualizarStatus(venda, StatusVenda.AGUARDANDO_PAG);
        } else {
            throw new IllegalArgumentException("Esta venda já foi confirmada.");
        }
        Venda salvo = vendaRepository.save(venda);
        publicarVendaRegistrada(salvo, salvo.getVendaProdutos(), loggedUser);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO pagarVenda(User loggedUser, Integer idVenda, Integer idPagamento) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        podePagarVenda(venda);

        pagamentoVendaService.registrarPagamento(loggedUser, venda, idPagamento);

        atualizarVendaPosPagamento(loggedUser, venda);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public PaymentResponseDTO iniciarPagamentoExterno(User loggedUser, Integer idVenda, PaymentRequestDTO request) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        podePagarVenda(venda);
        PaymentProvider provider = request != null && request.getProvider() != null
                ? request.getProvider()
                : PaymentProvider.STRIPE;
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .amount(Optional.ofNullable(venda.getValorPendente()).orElse(BigDecimal.ZERO))
                .currency("brl")
                .description("Venda " + idVenda)
                .provider(provider)
                .build();
        PaymentConfig config = paymentConfigService.getConfig(loggedUser.getOrganizationId(), provider);
        return paymentService.createPayment(req, config);
    }

    public VendaResponseDTO confirmarPagamentoExterno(User loggedUser, Integer idVenda, PaymentConfirmationDTO confirmDTO) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        podePagarVenda(venda);
        PaymentProvider provider = confirmDTO.getProvider() != null ? confirmDTO.getProvider() : PaymentProvider.STRIPE;
        PaymentConfig config = paymentConfigService.getConfig(loggedUser.getOrganizationId(), provider);
        PagamentoDTO pagamentoDTO = paymentService.confirmPayment(confirmDTO.getPaymentIntentId(), config);
        pagamentoVendaService.lancarPagamento(venda, pagamentoDTO);
        atualizarVendaPosPagamento(loggedUser, venda);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO lancarPagamentoVenda(User loggedUser, Integer idVenda, List<PagamentoDTO> pagamentoDTO) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        podePagarVenda(venda);

        for (PagamentoDTO pagamento : pagamentoDTO) {
            if (pagamento.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PAGO &&
                    pagamento.getDataPagamento() != null && pagamento.getDataPagamento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("A data de pagamento não pode ser no futuro.");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PENDENTE) {
                if (pagamento.getDataVencimento() == null) {
                    throw new IllegalArgumentException("Pagamentos pendentes devem informar uma data de vencimento.");
                }
                if (pagamento.getDataVencimento().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("Um pagamento pendente não pode ter vencimento no passado.");
                }
            }
            pagamentoVendaService.lancarPagamento(venda, pagamento);
        }

        atualizarVendaPosPagamento(loggedUser, venda);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }


    public VendaResponseDTO estornarVendaIntegral(User loggedUser, Integer idVenda) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Não é possível estornar uma venda cancelada.");
        }
        Optional.ofNullable(venda.getPagamentos()).orElseGet(List::of).forEach(pagamento -> {
            if (pagamento.getStatusPagamento() == StatusPagamento.PAGO) {
                pagamentoVendaService.estornarPagamento(loggedUser, pagamento);
            }
        });
        devolverProdutosParaEstoque(venda, "Estorno integral da venda #" + venda.getId());
        venda.setValorPendente(BigDecimal.ZERO);
        if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
            venda.setStatus(StatusVenda.AGUARDANDO_PAG);
        } else {
            atualizarStatus(venda, StatusVenda.AGUARDANDO_PAG);
        }
        atualizarStatus(venda, StatusVenda.CANCELADA);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO estornarPagamentoVenda(User loggedUser, Integer idVenda, Integer idPagamento) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirPagamentosHabilitados(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        VendaPagamento pagamento = pagamentoVendaService.listarUmPagamento(loggedUser, idPagamento);
        if (venda.getPagamentos().contains(pagamento)) {
            pagamentoVendaService.estornarPagamento(loggedUser, pagamento);
        } else {
            throw new IllegalArgumentException("O pagamento informado não pertence a esta venda.");
        }
        List<VendaPagamento> pagamentosRealizados = pagamentoVendaService.listarPagamentosRealizadosVenda(loggedUser, venda.getId());

        BigDecimal valorPago = pagamentosRealizados.stream()
                .map(VendaPagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorPendente = venda.getValorFinal().subtract(valorPago);
        if (valorPendente.compareTo(BigDecimal.ZERO) < 0) {
            valorPendente = BigDecimal.ZERO;
        }
        venda.setValorPendente(valorPendente);

        if (valorPendente.compareTo(BigDecimal.ZERO) <= 0) {
            if (venda.getStatus() != StatusVenda.CONCRETIZADA) {
                atualizarStatus(venda, StatusVenda.PAGA);
            }
        } else if (valorPago.compareTo(BigDecimal.ZERO) > 0) {
            if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
                venda.setStatus(StatusVenda.PARCIALMENTE_PAGA);
            } else {
                atualizarStatus(venda, StatusVenda.PARCIALMENTE_PAGA);
            }
        } else {
            if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
                venda.setStatus(StatusVenda.AGUARDANDO_PAG);
            } else {
                atualizarStatus(venda, StatusVenda.AGUARDANDO_PAG);
            }
        }
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO agendarVenda(User loggedUser, Integer idVenda, String dataAgendada, String horaAgendada,
                                         Integer duracaoMinutos) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirAgendaHabilitada(plano);
        Venda venda = getVenda(loggedUser, idVenda);

        if (venda.getVendaServicos().isEmpty()){
            throw new IllegalArgumentException("Não é possível agendar uma venda sem serviços.");
        }
        if (venda.getStatus() != StatusVenda.PENDENTE && venda.getStatus() != StatusVenda.PAGA) {
            throw new IllegalStateException(
                    "Não é possível agendar uma venda com status " + venda.getStatus() + ".");
        }

        LocalDate data = agendaValidator.validarDataAgendamento(dataAgendada);
        LocalTime hora = agendaValidator.validarHoraAgendada(horaAgendada);
        Duration duracao = agendaValidator.validarDuracao(duracaoMinutos);
        agendaValidator.validarConflitosAgendamentoVenda(loggedUser, venda, data, hora, duracao);

        venda.setDataAgendada(data);
        venda.setHoraAgendada(hora);
        venda.setDuracaoEstimada(duracao);
        venda.setStatus(StatusVenda.AGENDADA);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO finalizarAgendamentoVenda(User loggedUser, Integer idVenda) {
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirAgendaHabilitada(plano);
        Venda venda = getVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.AGENDADA) {
            BigDecimal valorPago = venda.getValorFinal().subtract(venda.getValorPendente());
            if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
                venda.setStatus(StatusVenda.AGUARDANDO_PAG);
            } else if (valorPago.compareTo(venda.getValorFinal()) < 0) {
                venda.setStatus(StatusVenda.PARCIALMENTE_PAGA);
            } else {
                venda.setStatus(StatusVenda.CONCRETIZADA);
            }
        }  else {
            throw new IllegalArgumentException("Não é possível finalizar um agendamento que não está agendado.");
        }
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    public VendaResponseDTO finalizarVenda(User loggedUser, Integer idVenda) {
        Venda venda = getVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.ORCAMENTO) {
            throw new IllegalArgumentException("Uma venda orçamento não pode ser finalizada.");
        } else if (venda.getValorPendente().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("A venda não pode ser finalizada enquanto houver saldo pendente.");
        }
        if (venda.getStatus() == StatusVenda.AGENDADA || venda.getStatus() == StatusVenda.PAGA) {
            venda.setStatus(StatusVenda.CONCRETIZADA);
        }
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "vendas", allEntries = true)
    public VendaResponseDTO cancelarVenda(User loggedUser, Integer idVenda) {
        Venda venda = getVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            return vendaMapper.toResponse(venda);
        }
        if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
            throw new IllegalArgumentException("Uma venda concretizada não pode ser cancelada.");
        }
        if (venda.getStatus() != StatusVenda.ORCAMENTO) {
            devolverProdutosParaEstoque(venda, "Cancelamento da venda #" + venda.getId());
        }
        venda.setValorPendente(BigDecimal.ZERO);
        venda.setStatus(StatusVenda.CANCELADA);
        Venda salvo = vendaRepository.save(venda);
        return vendaMapper.toResponse(salvo);
    }


    private List<VendaProduto> criarListaProdutos(List<VendaProdutoDTO> produtosDTO, Venda venda) {
        return produtosDTO.stream()
                .map(produtoDTO -> {
                    Produto produto = produtoService.buscarProdutoAtivo(produtoDTO.getProdutoId());
                    BigDecimal valorProduto = produto.getValorVenda()
                            .multiply(BigDecimal.valueOf(produtoDTO.getQuantidade()))
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal descontoPercentual = Optional.ofNullable(produtoDTO.getDesconto()).orElse(BigDecimal.ZERO);
                    BigDecimal descontoProduto = valorProduto.multiply(descontoPercentual)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal valorFinalProduto = valorProduto.subtract(descontoProduto)
                            .setScale(2, RoundingMode.HALF_UP);

                    VendaProduto vendaProduto = new VendaProduto();
                    vendaProduto.setVenda(venda);
                    vendaProduto.setProduto(produto);
                    vendaProduto.setValorUnitario(produto.getValorVenda());
                    vendaProduto.setQuantidade(produtoDTO.getQuantidade());
                    vendaProduto.setDesconto(descontoPercentual);
                    vendaProduto.setValorFinal(valorFinalProduto);
                    return vendaProduto;
                })
                .collect(Collectors.toList());
    }

    private void publicarVendaRegistrada(Venda venda, List<VendaProduto> itens, User owner) {
        if (venda == null || venda.getStatus() == StatusVenda.ORCAMENTO || itens == null || itens.isEmpty()) {
            return;
        }

        Integer organizationId = Optional.ofNullable(venda.getOrganizationId())
                .orElse(owner != null ? owner.getTenantId() : null);

        List<InventoryAdjustment> ajustes = itens.stream()
                .map(item -> new InventoryAdjustment(item.getProduto().getId(), item.getQuantidade()))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(new VendaRegistradaEvent(venda.getId(), organizationId, ajustes));
    }

    private List<VendaServico> criarListaServicos(List<VendaServicoDTO> servicosDTO, Venda venda) {
        return servicosDTO.stream()
            .map(servicoDTO -> {
                Servico servico = servicoService.buscarServicoAtivo(servicoDTO.getServicoId());
                BigDecimal valorServico = servico.getValorVenda()
                        .multiply(BigDecimal.valueOf(servicoDTO.getQuantidade()))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal descontoPercentual = Optional.ofNullable(servicoDTO.getDesconto()).orElse(BigDecimal.ZERO);
                BigDecimal descontoServico = valorServico.multiply(descontoPercentual)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal valorFinalServico = valorServico.subtract(descontoServico)
                        .setScale(2, RoundingMode.HALF_UP);

                VendaServico vendaServico = new VendaServico();
                vendaServico.setVenda(venda);
                vendaServico.setServico(servico);
                vendaServico.setValorUnitario(servico.getValorVenda());
                vendaServico.setQuantidade(servicoDTO.getQuantidade());
                vendaServico.setDesconto(descontoPercentual);
                vendaServico.setValorFinal(valorFinalServico);
                return vendaServico;
            })
            .collect(Collectors.toList());
    }

    public void atualizarStatus(Venda venda, StatusVenda novoStatus) {
        STATUS_TRANSITION_POLICY.validate(venda.getStatus(), novoStatus, venda);
        venda.setStatus(novoStatus);
    }

    public void podePagarVenda(Venda venda) {
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Não é possível pagar uma venda cancelada.");
        } else if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
            throw new IllegalArgumentException("Não é possível pagar uma venda concretizada.");
        } else if (venda.getStatus() == StatusVenda.PAGA || venda.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Esta venda já foi paga.");
        }
    }

    private void atualizarVendaPosPagamento(User loggedUser, Venda venda) {
        List<VendaPagamento> pagamentos = pagamentoVendaService.listarPagamentosRealizadosVenda(loggedUser, venda.getId());

        BigDecimal valorPago = pagamentos.stream().map(VendaPagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPendente = venda.getValorFinal().subtract(valorPago);
        if (valorPendente.compareTo(BigDecimal.ZERO) < 0) {
            valorPendente = BigDecimal.ZERO;
        }
        venda.setValorPendente(valorPendente);
        if (venda.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            if (venda.getStatus() == StatusVenda.AGUARDANDO_PAG || venda.getStatus() == StatusVenda.PENDENTE) {
                atualizarStatus(venda, StatusVenda.PAGA);
            }
        } else {
            if (venda.getStatus() == StatusVenda.AGUARDANDO_PAG) {
                atualizarStatus(venda, StatusVenda.PARCIALMENTE_PAGA);
            }
        }
    }

    private Plano obterPlanoAtivo(User loggedUser) {
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

    private void devolverProdutosParaEstoque(Venda venda, String descricao) {
        Optional.ofNullable(venda.getVendaProdutos()).orElseGet(List::of).forEach(vp ->
                inventoryService.incrementar(vp.getProduto().getId(), vp.getQuantidade(), InventorySource.VENDA,
                        venda.getId(), descricao));
    }
}
