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
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final FornecedorService fornecedorService;
    private final ContadorService contadorService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final CompraProdutoRepository compraProdutoRepository;
    private final CompraServicoRepository compraServicoRepository;
    private final PagamentoCompraService pagamentoCompraService;

    @Cacheable("compras")
    @Transactional(readOnly = true)
    public List<Compra> listarCompras(User loggedUser, CompraSearch pesquisa) {
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuário logado
        Page<Compra> compras = compraRepository.buscarCompras(
                loggedUser.getId(),
                pesquisa.getId(),
                pesquisa.getFornecedorId(),
                pesquisa.getDataInicial(),
                pesquisa.getDataFinal(),
                pesquisa.getStatus(),
                pesquisa.getPago(),
                pesquisa.getFormaPagamento(),
                pageable
        );
        
        return compras.getContent();
    }

    public Compra listarUmaCompra(User loggedUser, Integer idCompra) {
        return compraRepository.findByIdAndOwnerUser(idCompra, loggedUser)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
    }

    @Transactional
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
                .valorFinal(0.0)
                .condicaoPagamento(compraDTO.getCondicaoPagamento())
                .valorPendente(0.0)
                .status(compraDTO.getStatus())
                .observacoes(compraDTO.getObservacoes())
                .build();

        List<CompraProduto> compraProdutos = criarListaProdutos(compraDTO.getProdutos(), novaCompra);
        List<CompraServico> compraServicos = criarListaServicos(compraDTO.getServicos(), novaCompra);
        novaCompra.setCompraProdutos(compraProdutos);
        novaCompra.setCompraServicos(compraServicos);

        double valorTotal = compraProdutos.stream().mapToDouble(CompraProduto::getValorTotal).sum()
                + compraServicos.stream().mapToDouble(CompraServico::getValorTotal).sum();
        double valorPago = 0.0;

        novaCompra.setValorFinal(valorTotal);
        novaCompra.setValorPendente(valorTotal - valorPago);

        
        compraRepository.save(novaCompra);
        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);
        contadorService.IncrementarContador(Tabela.COMPRA, loggedUser);
        return novaCompra;
    }

    @Transactional
    public Compra editarCompra(User loggedUser, Integer idCompra, CompraDTO compraDTO) {
        validarCompra(compraDTO, loggedUser);

        Compra compra = listarUmaCompra(loggedUser, idCompra);
        Compra compraAtualizada = Compra.builder()
                .ownerUser(loggedUser)
                .fornecedor(compra.getFornecedor())
                .sequencialUsuario(compra.getSequencialUsuario())
                .dataEfetuacao(compraDTO.getDataEfetuacao())
                .dataAgendada(compraDTO.getDataAgendada())
                .valorFinal(0.0)
                .condicaoPagamento(compraDTO.getCondicaoPagamento())
                .valorPendente(0.0)
                .status(compraDTO.getStatus())
                .observacoes(compraDTO.getObservacoes())
                .build();

        compraProdutoRepository.deleteAll(compra.getCompraProdutos());
        compraServicoRepository.deleteAll(compra.getCompraServicos());

        List<CompraProduto> compraProdutos = criarListaProdutos(compraDTO.getProdutos(), compraAtualizada);
        List<CompraServico> compraServicos = criarListaServicos(compraDTO.getServicos(), compraAtualizada);
        compraAtualizada.setCompraProdutos(compraProdutos);
        compraAtualizada.setCompraServicos(compraServicos);

        double valorTotal = compraProdutos.stream().mapToDouble(CompraProduto::getValorTotal).sum()
                + compraServicos.stream().mapToDouble(CompraServico::getValorTotal).sum();

        double valorPago = compra.getPagamentos() == null
                ? 0.0
                : compra.getPagamentos().stream().mapToDouble(CompraPagamento::getValorPago).sum();

        compraAtualizada.setValorFinal(valorTotal);
        compraAtualizada.setValorPendente(valorTotal - valorPago);
        atualizarStatus(compra, compraAtualizada.getStatus());

        compraProdutoRepository.saveAll(compraProdutos);
        compraServicoRepository.saveAll(compraServicos);
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

        pagamentoCompraService.registrarPagamento(loggedUser, compra, idPagamento);

        atualizarCompraPosPagamento(compra);
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra lancarPagamentoCompra(User loggedUser, Integer idCompra, List<PagamentoDTO> pagamentoDTO) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        podePagarCompra(compra);

        for (PagamentoDTO pagamento : pagamentoDTO) {
            if (pagamento.getValorPago() <= 0) {
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
                        { pagamentoCompraService.estornarPagamento(loggedUser, pagamento); }
                    });
        return compraRepository.save(compra);
    }

    public Compra estornarPagamentoCompra(User loggedUser, Integer idCompra, Integer idPagamento) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        CompraPagamento pagamento = pagamentoCompraService.listarUmPagamento(loggedUser, idPagamento);
        if (compra.getPagamentos().contains(pagamento) && pagamento.getStatusPagamento() == StatusPagamento.PAGO) {
            pagamentoCompraService.estornarPagamento(loggedUser, pagamento);
        } else {
            throw new IllegalArgumentException("O pagamento informado não pode ser estornado");
        }
        double valorPago = compra.getPagamentos().stream().mapToDouble(CompraPagamento::getValorPago).sum();
        compra.setValorPendente(compra.getValorFinal() - valorPago);
        if (compra.getValorPendente() <= 0) {
            if (compra.getStatus() == StatusCompra.AGUARDANDO_PAG || compra.getStatus() == StatusCompra.PARCIALMENTE_PAGO) {
                atualizarStatus(compra, StatusCompra.PAGO);
            }
        } else if (valorPago < 0) {
            atualizarStatus(compra, StatusCompra.PARCIALMENTE_PAGO);
        }
        return compraRepository.save(compra);
    }

    public Compra finalizarCompra(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        atualizarStatus(compra, StatusCompra.CONCRETIZADO);
        return compraRepository.save(compra);
    }

    public Compra cancelarCompra(User loggedUser, Integer idCompra) {
        Compra compra = listarUmaCompra(loggedUser, idCompra);
        atualizarStatus(compra, StatusCompra.CANCELADO);
        return compraRepository.save(compra);
    }

    private List<CompraProduto> criarListaProdutos(List<CompraProdutoDTO> produtosDTO, Compra compra) {
        return produtosDTO.stream()
                .map(produtoDTO -> {
                    Produto produto = produtoService.listarUmProduto(compra.getOwnerUser(), produtoDTO.getProdutoId());
                    double valorFinalProduto = produto.getValorVenda() * produtoDTO.getQuantidade();

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
                    Servico servico = servicoService.listarUmServico(compra.getOwnerUser(), servicoDTO.getServicoId());
                    double valorFinalServico = servico.getValorVenda() * servicoDTO.getQuantidade();
                    
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
                if (compra.getValorPendente() > 0) {
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
        } else if (compra.getStatus() == StatusCompra.PAGO || compra.getValorPendente() <= 0) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para uma compra já paga");
        } else if (compra.getStatus() == StatusCompra.ORCAMENTO) {
            throw new IllegalArgumentException("Não é possível lançar um pagamento para um orçamento");
        }
    }

    public void atualizarCompraPosPagamento(Compra compra) {
        List<CompraPagamento> pagamentos = pagamentoCompraService.listarPagamentosRealizadosCompra(compra.getOwnerUser(), compra.getId());

        double valorPago = pagamentos.stream().mapToDouble(CompraPagamento::getValorPago).sum();
        compra.setValorPendente(compra.getValorFinal() - valorPago);

        if (compra.getValorPendente() == 0) {
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
