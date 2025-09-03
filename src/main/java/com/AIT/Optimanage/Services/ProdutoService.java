package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Mappers.ProdutoMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Cacheable(value = "produtos", key = "#loggedUser.id + '-' + #pesquisa.hashCode()")
    public Page<ProdutoResponse> listarProdutos(User loggedUser, Search pesquisa) {
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return produtoRepository.findAllByAtivoTrue(pageable)
                .map(this::toResponse);
    }

    public ProdutoResponse listarUmProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
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
        Produto produtoSalvo = buscarProdutoAtivo(idProduto);
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(produtoSalvo.getId());
        produto.setOwnerUser(produtoSalvo.getOwnerUser());
        Produto atualizado = produtoRepository.save(produto);
        return toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "#loggedUser.id")
    public void excluirProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    public ProdutoResponse restaurarProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProduto(idProduto);
        produto.setAtivo(true);
        Produto atualizado = produtoRepository.save(produto);
        return toResponse(atualizado);
    }

    public void removerProduto(User loggedUser, Integer idProduto) {
        Produto produto = buscarProduto(idProduto);
        produtoRepository.delete(produto);
    }

    private Produto buscarProduto(Integer idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public Produto buscarProdutoAtivo(Integer idProduto) {
        return produtoRepository.findByIdAndAtivoTrue(idProduto)
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
