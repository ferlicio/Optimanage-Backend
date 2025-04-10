package com.AIT.Optimanage.Services;

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

    public Produto cadastrarProduto(User loggedUser, Produto produto) {
        produto.setId(null);
        produto.setOwnerUser(loggedUser);
        return produtoRepository.save(produto);
    }

    public Produto editarProduto(User loggedUser, Integer idProduto, Produto produto) {
        Produto produtoSalvo = listarUmProduto(loggedUser, idProduto);
        produto.setId(produtoSalvo.getId());
        produto.setOwnerUser(produtoSalvo.getOwnerUser());
        return produtoRepository.save(produto);
    }

    public void excluirProduto(User loggedUser, Integer idProduto) {
        Produto produto = listarUmProduto(loggedUser, idProduto);
        produtoRepository.delete(produto);
    }
}
