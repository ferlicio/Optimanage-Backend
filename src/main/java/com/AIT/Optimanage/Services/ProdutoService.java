package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Mappers.ProdutoMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Cacheable(value = "produtos", key = "#loggedUser.id")
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

    @Transactional
    @CacheEvict(value = "produtos", key = "#loggedUser.id")
    public ProdutoResponse cadastrarProduto(User loggedUser, ProdutoRequest request) {
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(null);
        produto.setOwnerUser(loggedUser);
        Produto salvo = produtoRepository.save(produto);
        return toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "#loggedUser.id")
    public ProdutoResponse editarProduto(User loggedUser, Integer idProduto, ProdutoRequest request) {
        Produto produtoSalvo = buscarProdutoAtivo(loggedUser, idProduto);
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(produtoSalvo.getId());
        produto.setOwnerUser(produtoSalvo.getOwnerUser());
        Produto atualizado = produtoRepository.save(produto);
        return toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "#loggedUser.id")
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

    private Produto buscarProduto(User loggedUser, Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUser(idProduto, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public Produto buscarProdutoAtivo(User loggedUser, Integer idProduto) {
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
