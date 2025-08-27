package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<ProdutoResponse> listarProdutos(User loggedUser) {
        return produtoRepository.findAllByOwnerUserAndAtivoTrue(loggedUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProdutoResponse listarUmProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProdutoAtivo(loggedUser, idProduto);
        return toResponse(produto);
    }

    public ProdutoResponse cadastrarProduto(User loggedUser, ProdutoRequest request) {
        Produto produto = fromRequest(request);
        produto.setId(null);
        produto.setOwnerUser(loggedUser);
        Produto salvo = produtoRepository.save(produto);
        return toResponse(salvo);
    }

    public ProdutoResponse editarProduto(User loggedUser, Integer idProduto, ProdutoRequest request) {
        Produto produtoSalvo = buscarProdutoAtivo(loggedUser, idProduto);
        Produto produto = fromRequest(request);
        produto.setId(produtoSalvo.getId());
        produto.setOwnerUser(produtoSalvo.getOwnerUser());
        Produto atualizado = produtoRepository.save(produto);
        return toResponse(atualizado);
    }

    public void excluirProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProdutoAtivo(loggedUser, idProduto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    public ProdutoResponse restaurarProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProduto(loggedUser, idProduto);
        produto.setAtivo(true);
        Produto atualizado = produtoRepository.save(produto);
        return toResponse(atualizado);
    }

    public void removerProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProduto(loggedUser, idProduto);
        produtoRepository.delete(produto);
    }

    private Produto fromRequest(ProdutoRequest request) {
        Produto produto = new Produto();
        if (request.getFornecedorId() != null) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(request.getFornecedorId());
            produto.setFornecedor(fornecedor);
        }
        produto.setSequencialUsuario(request.getSequencialUsuario());
        produto.setCodigoReferencia(request.getCodigoReferencia());
        produto.setNome(request.getNome());
        produto.setDescricao(request.getDescricao());
        produto.setCusto(request.getCusto());
        produto.setDisponivelVenda(request.getDisponivelVenda());
        produto.setValorVenda(request.getValorVenda());
        produto.setQtdEstoque(request.getQtdEstoque());
        produto.setTerceirizado(request.getTerceirizado());
        produto.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
        return produto;
    }

    private Produto buscarProduto(User loggedUser, Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUser(idProduto, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    private Produto buscarProdutoAtivo(User loggedUser, Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUserAndAtivoTrue(idProduto, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    private ProdutoResponse toResponse(Produto produto) {
        return ProdutoResponse.builder()
                .id(produto.getId())
                .ownerUserId(produto.getOwnerUser() != null ? produto.getOwnerUser().getId() : null)
                .fornecedorId(produto.getFornecedor() != null ? produto.getFornecedor().getId() : null)
                .sequencialUsuario(produto.getSequencialUsuario())
                .codigoReferencia(produto.getCodigoReferencia())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .custo(produto.getCusto())
                .disponivelVenda(produto.getDisponivelVenda())
                .valorVenda(produto.getValorVenda())
                .qtdEstoque(produto.getQtdEstoque())
                .terceirizado(produto.getTerceirizado())
                .ativo(produto.getAtivo())
                .build();
    }
}
