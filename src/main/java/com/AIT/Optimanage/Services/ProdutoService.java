package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
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

    public List<Produto> listarProdutos(User loggedUser) {
        return produtoRepository.findAllByOwnerUser(loggedUser);
    }

    public Produto listarUmProduto(User loggedUser, Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUser(idProduto, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado")
        );
    }

    public Produto cadastrarProduto(User loggedUser, ProdutoRequest request) {
        Produto produto = fromRequest(request);
        produto.setId(null);
        produto.setOwnerUser(loggedUser);
        return produtoRepository.save(produto);
    }

    public Produto editarProduto(User loggedUser, Integer idProduto, ProdutoRequest request) {
        Produto produtoSalvo = listarUmProduto(loggedUser, idProduto);
        Produto produto = fromRequest(request);
        produto.setId(produtoSalvo.getId());
        produto.setOwnerUser(produtoSalvo.getOwnerUser());
        return produtoRepository.save(produto);
    }

    public void excluirProduto(User loggedUser, Integer idProduto) {
        Produto produto = listarUmProduto(loggedUser, idProduto);
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
        return produto;
    }
}
