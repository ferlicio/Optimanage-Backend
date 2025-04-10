package com.AIT.Optimanage.Services.Venda;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.StatusPagamento;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Servico;
import com.AIT.Optimanage.Models.User.Contador;
import com.AIT.Optimanage.Models.User.Tabela;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.PagamentoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoDTO;
import com.AIT.Optimanage.Models.Venda.Search.VendaSearch;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import com.AIT.Optimanage.Repositories.Venda.VendaProdutoRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaServicoRepository;
import com.AIT.Optimanage.Services.Cliente.ClienteService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.User.ContadorService;
import jakarta.persistence.EntityNotFoundException;
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
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final VendaProdutoRepository vendaProdutoRepository;
    private final VendaServicoRepository vendaServicoRepository;
    private final ContadorService contadorService;
    private final PagamentoVendaService pagamentoVendaService;

    @Cacheable("vendas")
    @Transactional(readOnly = true)
    public List<Venda> listarVendas(User loggedUser, VendaSearch pesquisa) {
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuario logado
        Page<Venda> vendas = vendaRepository.buscarVendas(
                loggedUser.getId(),
                pesquisa.getId(),
                pesquisa.getClienteId(),
                pesquisa.getDataInicial(),
                pesquisa.getDataFinal(),
                pesquisa.getStatus(),
                pesquisa.getPago(),
                pesquisa.getFormaPagamento(),
                pageable);

        return vendas.getContent();
    }

    public Venda listarUmaVenda(User loggedUser, Integer idVenda) {
        return vendaRepository.findByIdAndOwnerUser(idVenda, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada"));
    }

    @Transactional
    public Venda registrarVenda(User loggedUser, VendaDTO vendaDTO) {
        validarVenda(vendaDTO, loggedUser);

        Cliente cliente = clienteService.listarUmCliente(loggedUser, vendaDTO.getClienteId());
        Contador contador = contadorService.BuscarContador(Tabela.VENDA, loggedUser);
        Venda novaVenda = Venda.builder()
                .ownerUser(loggedUser)
                .cliente(cliente)
                .sequencialUsuario(contador.getContagemAtual())
                .dataEfetuacao(vendaDTO.getDataEfetuacao())
                .dataAgendada(vendaDTO.getDataAgendada())
                .dataCobranca(vendaDTO.getDataCobranca())
                .valorTotal(0.0)
                .descontoGeral(vendaDTO.getDescontoGeral())
                .valorFinal(0.0)
                .condicaoPagamento(vendaDTO.getCondicaoPagamento())
                .alteracoesPermitidas(vendaDTO.getAlteracoesPermitidas())
                .valorPendente(0.0)
                .status(vendaDTO.getStatus())
                .observacoes(vendaDTO.getObservacoes())
                .build();

        List<VendaProduto> vendaProdutos = criarListaProdutos(vendaDTO.getProdutos(), novaVenda);
        List<VendaServico> vendaServicos = criarListaServicos(vendaDTO.getServicos(), novaVenda);

        double valorTotal = vendaProdutos.stream().mapToDouble(VendaProduto::getValorFinal).sum()
                + vendaServicos.stream().mapToDouble(VendaServico::getValorFinal).sum();

        double valorPago = 0.0;

        novaVenda.setValorTotal(valorTotal);
        novaVenda.setValorFinal(valorTotal - novaVenda.getDescontoGeral());
        novaVenda.setValorPendente(novaVenda.getValorFinal() - valorPago);
        novaVenda.setVendaProdutos(vendaProdutos);
        novaVenda.setVendaServicos(vendaServicos);

        vendaRepository.save(novaVenda);
        vendaProdutoRepository.saveAll(vendaProdutos);
        vendaServicoRepository.saveAll(vendaServicos);
        contadorService.IncrementarContador(Tabela.VENDA, loggedUser);
        return novaVenda;
    }

    @Transactional
    public Venda atualizarVenda(User loggedUser, Integer vendaId, VendaDTO vendaDTO) {
        validarVenda(vendaDTO, loggedUser);

        Venda venda = listarUmaVenda(loggedUser, vendaId);
        Venda vendaAtualizada = Venda.builder()
                .ownerUser(loggedUser)
                .cliente(venda.getCliente())
                .sequencialUsuario(venda.getSequencialUsuario())
                .dataEfetuacao(vendaDTO.getDataEfetuacao())
                .dataAgendada(vendaDTO.getDataAgendada())
                .dataCobranca(vendaDTO.getDataCobranca())
                .condicaoPagamento(vendaDTO.getCondicaoPagamento())
                .alteracoesPermitidas(vendaDTO.getAlteracoesPermitidas())
                .valorPendente(venda.getValorFinal())
                .status(vendaDTO.getStatus())
                .observacoes(vendaDTO.getObservacoes())
                .build();

        // Remove os produtos e serviços antigos
        vendaProdutoRepository.deleteByVenda(venda);
        vendaServicoRepository.deleteByVenda(venda);

        List<VendaProduto> vendaProdutos = criarListaProdutos(vendaDTO.getProdutos(), vendaAtualizada);
        List<VendaServico> vendaServicos = criarListaServicos(vendaDTO.getServicos(), vendaAtualizada);

        double valorTotal = vendaProdutos.stream().mapToDouble(VendaProduto::getValorFinal).sum()
                + vendaServicos.stream().mapToDouble(VendaServico::getValorFinal).sum();

        double valorPago = venda.getPagamentos() == null
                ? 0.0
                : venda.getPagamentos().stream().mapToDouble(VendaPagamento::getValorPago).sum();

        vendaAtualizada.setValorTotal(valorTotal);
        vendaAtualizada.setValorFinal(valorTotal * (100 - vendaAtualizada.getDescontoGeral())/100);
        vendaAtualizada.setValorPendente(vendaAtualizada.getValorFinal() - valorPago);
        vendaAtualizada.setVendaProdutos(vendaProdutos);
        vendaAtualizada.setVendaServicos(vendaServicos);
        atualizarStatus(venda, vendaAtualizada.getStatus());

        vendaProdutoRepository.saveAll(vendaProdutos);
        vendaServicoRepository.saveAll(vendaServicos);
        return vendaRepository.save(venda);
    }

    public Venda confirmarVenda(User loggedUser, Integer idVenda) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.ORCAMENTO && venda.getVendaServicos().isEmpty()) {
            atualizarStatus(venda, StatusVenda.PENDENTE);
        } else if (venda.getStatus() == StatusVenda.ORCAMENTO) {
            atualizarStatus(venda, StatusVenda.AGUARDANDO_PAG);
        } else {
            throw new IllegalArgumentException("Esta venda já foi confirmada.");
        }
        return vendaRepository.save(venda);
    }

    public Venda pagarVenda(User loggedUser, Integer idVenda, Integer idPagamento) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        podePagarVenda(venda);

        pagamentoVendaService.registrarPagamento(loggedUser, venda, idPagamento);

        atualizarVendaPosPagamento(venda);
        return vendaRepository.save(venda);
    }

    public Venda lancarPagamentoVenda(User loggedUser, Integer idVenda, List<PagamentoDTO> pagamentoDTO) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        podePagarVenda(venda);

        for (PagamentoDTO pagamento : pagamentoDTO) {
            if (pagamento.getValorPago() <= 0) {
                throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
            } else if (pagamento.getDataPagamento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("A data de pagamento não pode ser no futuro.");
            }
            pagamentoVendaService.lancarPagamento(venda, pagamento);
        }

        atualizarVendaPosPagamento(venda);
        return vendaRepository.save(venda);
    }


    public Venda estornarVendaIntegral(User loggedUser, Integer idVenda) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.CONCRETIZADA || venda.getStatus() == StatusVenda.PAGA) {
            venda.setStatus(StatusVenda.AGUARDANDO_PAG);
        }
        venda.setValorPendente(venda.getValorFinal());
        venda.getPagamentos().forEach( pagamento
                -> { if (pagamento.getStatusPagamento() == StatusPagamento.PAGO)
                        { pagamentoVendaService.estornarPagamento(loggedUser, pagamento); }
                });
        return vendaRepository.save(venda);
    }

    public Venda estornarPagamentoVenda(User loggedUser, Integer idVenda, Integer idPagamento) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        VendaPagamento pagamento = pagamentoVendaService.listarUmPagamento(loggedUser, idPagamento);
        if (venda.getPagamentos().contains(pagamento)) {
            pagamentoVendaService.estornarPagamento(loggedUser, pagamento);
        } else {
            throw new IllegalArgumentException("O pagamento informado não pertence a esta venda.");
        }
        double valorPago = venda.getPagamentos().stream().mapToDouble(VendaPagamento::getValorPago).sum();
        venda.setValorPendente(venda.getValorFinal() - valorPago);
        if (venda.getValorPendente() <= 0) {
            if (venda.getStatus() == StatusVenda.AGUARDANDO_PAG || venda.getStatus() == StatusVenda.PENDENTE) {
                atualizarStatus(venda, StatusVenda.PAGA);
            }
        } else if (valorPago > 0) {
            atualizarStatus(venda, StatusVenda.PARCIALMENTE_PAGA);
        }
        return vendaRepository.save(venda);
    }

    public Venda agendarVenda(User loggedUser, Integer idVenda, String dataAgendada) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);

        if (venda.getVendaServicos().isEmpty()){
            throw new IllegalArgumentException("Não é possível agendar uma venda sem serviços.");
        }
        if (venda.getStatus() == StatusVenda.PENDENTE || venda.getStatus() == StatusVenda.PAGA) {
            venda.setDataAgendada(LocalDate.parse(dataAgendada));
            venda.setStatus(StatusVenda.AGENDADA);
        }
        return vendaRepository.save(venda);
    }

    public Venda finalizarAgendamentoVenda(User loggedUser, Integer idVenda) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.AGENDADA) {
            double valorPago = venda.getValorFinal() - venda.getValorPendente();
            if (valorPago <= 0) {
                venda.setStatus(StatusVenda.AGUARDANDO_PAG);
            } else if ( valorPago < venda.getValorFinal()) {
                venda.setStatus(StatusVenda.PARCIALMENTE_PAGA);
            } else {
                venda.setStatus(StatusVenda.CONCRETIZADA);
            }
        }  else {
            throw new IllegalArgumentException("Não é possível finalizar um agendamento que não está agendado.");
        }
        return vendaRepository.save(venda);
    }

    public Venda finalizarVenda(User loggedUser, Integer idVenda) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
         if (venda.getStatus() == StatusVenda.ORCAMENTO) {
            throw new IllegalArgumentException("Uma venda orçamento não pode ser finalizada.");
        } else if (venda.getValorPendente() > 0) {
            throw new IllegalArgumentException("A venda não pode ser finalizada enquanto houver saldo pendente.");
        }
        if (venda.getStatus() == StatusVenda.AGENDADA || venda.getStatus() == StatusVenda.PAGA) {
            venda.setStatus(StatusVenda.CONCRETIZADA);
        }
        return vendaRepository.save(venda);
    }

    public Venda cancelarVenda(User loggedUser, Integer idVenda) {
        Venda venda = listarUmaVenda(loggedUser, idVenda);
        if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
            throw new IllegalArgumentException("Uma venda concretizada não pode ser cancelada.");
        }
        venda.setStatus(StatusVenda.CANCELADA);
        return vendaRepository.save(venda);
    }


    private List<VendaProduto> criarListaProdutos(List<VendaProdutoDTO> produtosDTO, Venda venda) {
        return produtosDTO.stream()
                .map(produtoDTO -> {
                    Produto produto = produtoService.listarUmProduto(venda.getOwnerUser(), produtoDTO.getProdutoId());
                    double valorProduto = produto.getValorVenda() * produtoDTO.getQuantidade();
                    double descontoProduto = (produtoDTO.getDesconto() / 100.0) * valorProduto;
                    double valorFinalProduto = valorProduto - descontoProduto;

                    return VendaProduto.builder()
                            .venda(venda)
                            .produto(produto)
                            .valorUnitario(produto.getValorVenda())
                            .quantidade(produtoDTO.getQuantidade())
                            .desconto(produtoDTO.getDesconto())
                            .valorFinal(valorFinalProduto)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<VendaServico> criarListaServicos(List<VendaServicoDTO> servicosDTO, Venda venda) {
        return servicosDTO.stream()
            .map(servicoDTO -> {
                Servico servico = servicoService.listarUmServico(venda.getOwnerUser(), servicoDTO.getServicoId());
                double valorServico = servico.getValorVenda() * servicoDTO.getQuantidade();
                double descontoServico = (servicoDTO.getDesconto() / 100.0) * valorServico;
                double valorFinalServico = valorServico - descontoServico;

                return VendaServico.builder()
                        .venda(venda)
                        .servico(servico)
                        .valorUnitario(servico.getValorVenda())
                        .quantidade(servicoDTO.getQuantidade())
                        .desconto(servicoDTO.getDesconto())
                        .valorFinal(valorFinalServico)
                        .build();
            })
            .collect(Collectors.toList());
    }

    private void validarVenda(VendaDTO vendaDTO, User loggedUser ) {
        if (vendaDTO.getDataEfetuacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efetuação não pode ser no futuro");
        }
        if (vendaDTO.getDataAgendada() == null && vendaDTO.getStatus() == StatusVenda.AGENDADA) {
            throw new IllegalArgumentException("Data agendada não informada para venda agendada");
        }
        if (vendaDTO.getDataCobranca() == null) {
            if (vendaDTO.getStatus() == StatusVenda.AGUARDANDO_PAG) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda aguardando pagamento");
            } else if (vendaDTO.getStatus() == StatusVenda.CONCRETIZADA) {
                throw new IllegalArgumentException("Data de cobrança não informada para venda concretizada");
            }
        }
        if (vendaDTO.getStatus() == null) {
            throw new IllegalArgumentException("Status não informado");
        } else if (vendaDTO.getStatus() == StatusVenda.ORCAMENTO && !loggedUser.getUserInfo().getPermiteOrcamento()) {
            throw new IllegalArgumentException("Usuário não tem permissão para criar orçamentos");
        }
        if (vendaDTO.hasNoItems()) {
            throw new IllegalArgumentException("Uma venda deve ter no mínimo um produto ou serviço");
        }
    }

    public void atualizarStatus(Venda venda, StatusVenda novoStatus) {
        StatusVenda statusAtual = venda.getStatus();

        if (statusAtual == novoStatus) {
            throw new IllegalStateException("A venda já está neste status.");
        }

        switch (novoStatus) {
            case ORCAMENTO:
                throw new IllegalStateException("Não é possível voltar para o status ORÇAMENTO.");

            case PENDENTE:
                if (statusAtual != StatusVenda.ORCAMENTO) {
                    throw new IllegalStateException("Só é possível transformar um orçamento em venda a partir do status ORCAMENTO.");
                }

            case AGENDADA:
                if (statusAtual == StatusVenda.CONCRETIZADA || statusAtual == StatusVenda.CANCELADA) {
                    throw new IllegalStateException("Não é possivel agendar uma venda cancelada ou concretizada.");
                }
                break;

            case AGUARDANDO_PAG:
                if (statusAtual == StatusVenda.CONCRETIZADA || statusAtual == StatusVenda.CANCELADA) {
                    throw new IllegalStateException("Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de aguardando pagamento.");
                }
                break;

            case PARCIALMENTE_PAGA:
                if (statusAtual == StatusVenda.CONCRETIZADA || statusAtual == StatusVenda.CANCELADA) {
                    throw new IllegalStateException("Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de parcialmente paga.");
                }
                if (venda.getValorFinal().equals(venda.getValorPendente()) || venda.getPagamentos().isEmpty()) {
                    throw new IllegalStateException("A venda não pode ser parcialmente paga sem um pagamento anterior.");
                }
                break;

            case PAGA:
                if (statusAtual == StatusVenda.CONCRETIZADA || statusAtual == StatusVenda.CANCELADA) {
                    throw new IllegalStateException("Uma venda CONCRETIZADA ou CANCELADA não pode voltar para o estado de paga.");
                }
                if (venda.getValorPendente() > 0) {
                    throw new IllegalStateException("A venda não pode ser paga sem o pagamento completo.");
                }
                break;

            case CONCRETIZADA:
                if (statusAtual == StatusVenda.CANCELADA) {
                    throw new IllegalStateException("Uma venda cancelada não pode ser concretizada.");
                }
                if (statusAtual == StatusVenda.ORCAMENTO) {
                    throw new IllegalStateException("Um orçamento não pode ser concretizado.");
                }
                if (venda.getValorPendente() > 0) {
                    throw new IllegalStateException("A venda não pode ser concretizada sem o pagamento completo.");
                }
                break;

            case CANCELADA:
                if (statusAtual == StatusVenda.CONCRETIZADA) {
                    throw new IllegalStateException("Uma venda concretizada não pode ser cancelada.");
                }
                break;

            default:
                throw new IllegalArgumentException("Status desconhecido.");
        }

        venda.setStatus(novoStatus);
    }

    public void podePagarVenda(Venda venda) {
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Não é possível pagar uma venda cancelada.");
        } else if (venda.getStatus() == StatusVenda.CONCRETIZADA) {
            throw new IllegalArgumentException("Não é possível pagar uma venda concretizada.");
        } else if (venda.getStatus() == StatusVenda.PAGA || venda.getValorPendente() <= 0) {
            throw new IllegalArgumentException("Esta venda já foi paga.");
        }
    }

    private void atualizarVendaPosPagamento(Venda venda) {
        List<VendaPagamento> pagamentos = pagamentoVendaService.listarPagamentosRealizadosVenda(venda.getOwnerUser(), venda.getId());

        double valorPago = pagamentos.stream().mapToDouble(VendaPagamento::getValorPago).sum();
        venda.setValorPendente(venda.getValorFinal() - valorPago);
        if (venda.getValorPendente() <= 0) {
            if (venda.getStatus() == StatusVenda.AGUARDANDO_PAG || venda.getStatus() == StatusVenda.PENDENTE) {
                atualizarStatus(venda, StatusVenda.PAGA);
            }
        } else {
            if (venda.getStatus() == StatusVenda.AGUARDANDO_PAG) {
                atualizarStatus(venda, StatusVenda.PARCIALMENTE_PAGA);
            }
        }
    }
}
