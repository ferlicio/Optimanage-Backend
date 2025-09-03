package com.AIT.Optimanage.Services.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import com.AIT.Optimanage.Models.Compra.CompraServico;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraProdutoDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraServicoDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Compra.Search.CompraSearch;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Compra.CompraProdutoRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Compra.CompraServicoRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private final CompraRepository compraRepository;
    private final FornecedorService fornecedorService;
    private final ContadorService contadorService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final CompraProdutoRepository compraProdutoRepository;
    private final CompraServicoRepository compraServicoRepository;
    private final PagamentoCompraService pagamentoCompraService;
    private final ProdutoRepository produtoRepository;

    @Cacheable(value = "compras", key = "#loggedUser.id + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<Compra> listarCompras(User loggedUser, CompraSearch pesquisa) {
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos
        return compraRepository.buscarCompras(
                pesquisa.getId(),
                pesquisa.getFornecedorId(),
                pesquisa.getDataInicial(),
                pesquisa.getDataFinal(),
                pesquisa.getStatus(),
                pesquisa.getPago(),
                pesquisa.getFormaPagamento(),
                pageable
        );
    }

    public Compra listarUmaCompra(User loggedUser, Integer idCompra) {
        return compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
    }

    @Transactional
    @CacheEvict(value = "compras", allEntries = true)
    public Compra criarCompra(User loggedUser, CompraDTO compraDTO) {
        validarCompra(compraDTO, loggedUser);

        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(loggedUser, compraDTO.getFornecedorId());
        Contador contador = contadorService.BuscarContador(Tabela.COMPRA, loggedUser);
        Compra novaCompra = Compra.builder()
                .ownerUser(loggedUser)
                .fornecedor(fornecedor)
                .sequencialUsuario(contador.getContagemAtual())
                .dataEfetuacao(compraDTO.getDataEfetuacao())
                .dataAgendada(compraDTO.getDataAgendada())
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

        
        compraRepository.save(novaCompra);
        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);

        compraProdutos.forEach(cp -> {
            log.info("Incrementando estoque do produto {} em {} unidades", cp.getProduto().getId(), cp.getQuantidade());
            produtoRepository.incrementarEstoque(cp.getProduto().getId(), cp.getQuantidade());
        });

        contadorService.IncrementarContador(Tabela.COMPRA, loggedUser);
        return novaCompra;
    }

    @Transactional
    @CacheEvict(value = "compras", allEntries = true)
    public Compra editarCompra(User loggedUser, Integer idCompra, CompraDTO compraDTO) {
        validarCompra(compraDTO, loggedUser);

        Compra compra = listarUmaCompra(loggedUser, idCompra);
        Compra compraAtualizada = Compra.builder()
                .ownerUser(loggedUser)
                .fornecedor(compra.getFornecedor())
                .sequencialUsuario(compra.getSequencialUsuario())
                .dataEfetuacao(compraDTO.getDataEfetuacao())
                .dataAgendada(compraDTO.getDataAgendada())
                .valorFinal(BigDecimal.ZERO)
                .condicaoPagamento(compraDTO.getCondicaoPagamento())
                .valorPendente(BigDecimal.ZERO)
                .status(compraDTO.getStatus())
                .observacoes(compraDTO.getObservacoes())
                .build();

        compra.getCompraProdutos().forEach(cp -> {
            log.info("Revertendo estoque do produto {} em {} unidades", cp.getProduto().getId(), cp.getQuantidade());
            int updated = produtoRepository.reduzirEstoque(cp.getProduto().getId(), cp.getQuantidade());
            if (updated == 0) {
                log.warn("Estoque insuficiente para reverter produto {}", cp.getProduto().getId());
                throw new IllegalArgumentException("Estoque insuficiente para reverter produto " + cp.getProduto().getNome());
            }
        });

        compraProdutoRepository.deleteAll(compra.getCompraProdutos());
        compraServicoRepository.deleteAll(compra.getCompraServicos());

        List<CompraProduto> compraProdutos = criarListaProdutos(compraDTO.getProdutos(), compraAtualizada);
        List<CompraServico> compraServicos = criarListaServicos(compraDTO.getServicos(), compraAtualizada);
        compraAtualizada.setCompraProdutos(compraProdutos);
        compraAtualizada.setCompraServicos(compraServicos);

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

        compraAtualizada.setValorFinal(valorTotal);
        compraAtualizada.setValorPendente(valorTotal.subtract(valorPago));
        atualizarStatus(compra, compraAtualizada.getStatus());

        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);

        compraProdutos.forEach(cp -> {
            log.info("Incrementando estoque do produto {} em {} unidades", cp.getProduto().getId(), cp.getQuantidade());
            produtoRepository.incrementarEstoque(cp.getProduto().getId(), cp.getQuantidade());
        });

        return compraRepository.save(compra);
    }

    public Compra confirmarCompra(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        if (compra.getStatus() == StatusCompra.ORCAMENTO && compra.getCompraServicos().isEmpty()) {
            atualizarStatus(compra, StatusCompra.AGUARDANDO_PAG);
        } else {
            atualizarStatus(compra, StatusCompra.AGUARDANDO_EXECUCAO);
        }
        return compraRepository.save(compra);
    }

    public Compra pagarCompra(User loggedUser, Integer idCompra, Integer idPagamento) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        podePagarCompra(compra);

        pagamentoCompraService.registrarPagamento(compra, idPagamento);

        atualizarCompraPosPagamento(compra);
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra lancarPagamentoCompra(User loggedUser, Integer idCompra, List<PagamentoDTO> pagamentoDTO) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        podePagarCompra(compra);

        for (PagamentoDTO pagamento : pagamentoDTO) {
            if (pagamento.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor de pagamento deve ser maior que zero");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PAGO && pagamento.getDataPagamento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Um pagamento realizado não pode ser no futuro");
            } else if (pagamento.getStatusPagamento() == StatusPagamento.PENDENTE && pagamento.getDataPagamento().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Um pagamento pendente não pode ser no passado");
            }
            pagamentoCompraService.lancarPagamento(compra, pagamento);
        }

        atualizarCompraPosPagamento(compra);
        return compraRepository.save(compra);
    }

    public Compra estornarCompraIntegral(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        if (compra.getStatus() == StatusCompra.CONCRETIZADO || compra.getStatus() == StatusCompra.PAGO) {
            atualizarStatus(compra, StatusCompra.AGUARDANDO_PAG);
        }
        compra.setValorPendente(compra.getValorFinal());
        compra.getPagamentos().forEach(pagamento
                -> { if (pagamento.getStatusPagamento() == StatusPagamento.PAGO)
                        { pagamentoCompraService.estornarPagamento(pagamento); }
                    });
        return compraRepository.save(compra);
    }

    public Compra estornarPagamentoCompra(User loggedUser, Integer idCompra, Integer idPagamento) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        CompraPagamento pagamento = pagamentoCompraService.listarUmPagamento(compra, idPagamento);
        if (compra.getPagamentos().contains(pagamento) && pagamento.getStatusPagamento() == StatusPagamento.PAGO) {
            pagamentoCompraService.estornarPagamento(pagamento);
        } else {
            throw new IllegalArgumentException("O pagamento informado não pode ser estornado");
        }
        BigDecimal valorPago = compra.getPagamentos().stream()
                .map(CompraPagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setValorPendente(compra.getValorFinal().subtract(valorPago));
        if (compra.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            if (compra.getStatus() == StatusCompra.AGUARDANDO_PAG || compra.getStatus() == StatusCompra.PARCIALMENTE_PAGO) {
                atualizarStatus(compra, StatusCompra.PAGO);
            }
        } else if (valorPago.compareTo(BigDecimal.ZERO) < 0) {
            atualizarStatus(compra, StatusCompra.PARCIALMENTE_PAGO);
        }
        return compraRepository.save(compra);
    }

    public Compra finalizarCompra(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        atualizarStatus(compra, StatusCompra.CONCRETIZADO);
        return compraRepository.save(compra);
    }

    @CacheEvict(value = "compras", allEntries = true)
    public Compra cancelarCompra(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        atualizarStatus(compra, StatusCompra.CANCELADO);
        return compraRepository.save(compra);
    }

    private List<CompraProduto> criarListaProdutos(List<CompraProdutoDTO> produtosDTO, Compra compra) {
        return produtosDTO.stream()
                .map(produtoDTO -> {
                    Produto produto = produtoService.buscarProdutoAtivo(produtoDTO.getProdutoId());
                    BigDecimal valorFinalProduto = produto.getValorVenda()
                            .multiply(BigDecimal.valueOf(produtoDTO.getQuantidade()));

                    return CompraProduto.builder()
                            .compra(compra)
                            .produto(produto)
                            .valorUnitario(produto.getValorVenda())
                            .quantidade(produtoDTO.getQuantidade())
                            .valorTotal(valorFinalProduto)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CompraServico> criarListaServicos(List<CompraServicoDTO> servicosDTO, Compra compra) {
        return servicosDTO.stream()
                .map(servicoDTO -> {
                    Servico servico = servicoService.buscarServicoAtivo(servicoDTO.getServicoId());
                    BigDecimal valorFinalServico = servico.getValorVenda()
                            .multiply(BigDecimal.valueOf(servicoDTO.getQuantidade()));

                    return CompraServico.builder()
                            .compra(compra)
                            .servico(servico)
                            .valorUnitario(servico.getValorVenda())
                            .quantidade(servicoDTO.getQuantidade())
                            .valorTotal(valorFinalServico)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void validarCompra(CompraDTO compraDTO, User loggedUser) {
        if (compraDTO.getDataEfetuacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efetuação não pode ser no futuro");
        }
        if (compraDTO.getDataCobranca() == null) {
            if (compraDTO.getStatus() == StatusCompra.AGUARDANDO_PAG) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda aguardando pagamento");
            } else if (compraDTO.getStatus() == StatusCompra.CONCRETIZADO) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda concretizada");
            }
        }
        if (compraDTO.getStatus() == null) {
            throw new IllegalArgumentException("Status não informado");
        } else if (compraDTO.getStatus() == StatusCompra.ORCAMENTO && !loggedUser.getUserInfo().getPermiteOrcamento()) {
            throw new IllegalArgumentException("Usuário não tem permissão para criar orçamentos");
        }
        if (compraDTO.hasNoItems()) {
            throw new IllegalArgumentException("Uma venda deve ter no mínimo um produto ou serviço");
        }
    }

    private void atualizarStatus(Compra compra, StatusCompra novoStatus) {
        StatusCompra statusAtual = compra.getStatus();

        if (statusAtual == novoStatus) {
            throw new IllegalStateException("A compra já está neste status.");
        }

        switch (novoStatus) {
            case ORCAMENTO:
                throw new IllegalStateException("Não é possível voltar para o status ORÇAMENTO.");

            case AGUARDANDO_EXECUCAO:
                if (statusAtual != StatusCompra.ORCAMENTO) {
                    throw new IllegalStateException("Só é possível transformar um orçamento em pedido se estiver no status ORÇAMENTO.");
                }
                break;

            case AGUARDANDO_PAG:
                if (statusAtual != StatusCompra.AGUARDANDO_EXECUCAO && statusAtual != StatusCompra.ORCAMENTO) {
                    throw new IllegalStateException("A compra só pode aguardar pagamento se o pedido já foi realizado.");
                }
                break;

            case PARCIALMENTE_PAGO:
                if (statusAtual != StatusCompra.AGUARDANDO_PAG && statusAtual != StatusCompra.AGUARDANDO_EXECUCAO) {
                    throw new IllegalStateException("Uma compra só pode ser parcialmente paga se estiver aguardando pagamento ou já parcialmente paga.");
                }
                break;

            case PAGO:
                if (statusAtual != StatusCompra.AGUARDANDO_PAG && statusAtual != StatusCompra.PARCIALMENTE_PAGO) {
                    throw new IllegalStateException("A compra só pode ser paga se estiver aguardando pagamento ou parcialmente paga.");
                }
                break;

            case CONCRETIZADO:
                if (statusAtual != StatusCompra.PAGO && statusAtual != StatusCompra.AGUARDANDO_EXECUCAO) {
                    throw new IllegalStateException("A compra só pode ser finalizada se estiver paga ou aguardando execução.");
                }
                if (compra.getValorPendente().compareTo(BigDecimal.ZERO) > 0) {
                    throw new IllegalStateException("A compra não pode ser finalizada enquanto houver saldo pendente.");
                }
                break;

            case CANCELADO:
                if (statusAtual == StatusCompra.CONCRETIZADO) {
                    throw new IllegalStateException("Uma compra finalizada não pode ser cancelada.");
                }
                break;

            default:
                throw new IllegalArgumentException("Status desconhecido.");
        }

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

        BigDecimal valorPago = pagamentos.stream().map(CompraPagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setValorPendente(compra.getValorFinal().subtract(valorPago));

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

}
