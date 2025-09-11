package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
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

    @Cacheable(value = "produtos", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId() + '-' + #pesquisa.hashCode()")
    public Page<ProdutoResponse> listarProdutos(Search pesquisa) {
        User loggedUser = CurrentUser.get();
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return produtoRepository.findAllByOwnerUserAndAtivoTrue(loggedUser, pageable)
                .map(produtoMapper::toResponse);
    }

    public ProdutoResponse listarUmProduto(Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId()")
    public ProdutoResponse cadastrarProduto(ProdutoRequest request) {
        User loggedUser = CurrentUser.get();
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(null);
        Produto salvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId()")
    public ProdutoResponse editarProduto(Integer idProduto, ProdutoRequest request) {
        Produto produtoSalvo = buscarProdutoAtivo(idProduto);
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(produtoSalvo.getId());
        Produto atualizado = produtoRepository.save(produto);
        return produtoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId()")
    public void excluirProduto(Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    public ProdutoResponse restaurarProduto(Integer idProduto) {
        Produto produto = buscarProduto(idProduto);
        produto.setAtivo(true);
        Produto atualizado = produtoRepository.save(produto);
        return produtoMapper.toResponse(atualizado);
    }

    public void removerProduto(Integer idProduto) {
        Produto produto = buscarProduto(idProduto);
        produtoRepository.delete(produto);
    }

    private Produto buscarProduto(Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUser(idProduto, CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public Produto buscarProdutoAtivo(Integer idProduto) {
        return produtoRepository.findByIdAndOwnerUserAndAtivoTrue(idProduto, CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    // Mapping handled by ProdutoMapper
}
