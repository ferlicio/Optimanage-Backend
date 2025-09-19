package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ProdutoResponse;
import com.AIT.Optimanage.Mappers.ProdutoMapper;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.Produto;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
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
    private final PlanoService planoService;

    @Cacheable(value = "produtos", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId() + '-' + #pesquisa.hashCode()")
    public Page<ProdutoResponse> listarProdutos(Search pesquisa) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }

        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        return produtoRepository.findAllByOrganizationIdAndAtivoTrue(organizationId, pageable)
                .map(produtoMapper::toResponse);
    }

    public ProdutoResponse listarUmProduto(Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public ProdutoResponse cadastrarProduto(ProdutoRequest request) {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Plano plano = planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        long produtosAtivos = produtoRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteProdutos(plano, produtosAtivos, 1);

        Produto produto = produtoMapper.toEntity(request);
        produto.setId(null);
        produto.setTenantId(organizationId);
        Produto salvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public ProdutoResponse editarProduto(Integer idProduto, ProdutoRequest request) {
        Produto produtoSalvo = buscarProdutoAtivo(idProduto);
        Produto produto = produtoMapper.toEntity(request);
        produto.setId(produtoSalvo.getId());
        produto.setTenantId(produtoSalvo.getOrganizationId());
        Produto atualizado = produtoRepository.save(produto);
        return produtoMapper.toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public void excluirProduto(Integer idProduto) {
        Produto produto = buscarProdutoAtivo(idProduto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    public ProdutoResponse restaurarProduto(Integer idProduto) {
        User loggedUser = CurrentUser.get();
        if (loggedUser == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Plano plano = planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
        long produtosAtivos = produtoRepository.countByOrganizationIdAndAtivoTrue(organizationId);
        validarLimiteProdutos(plano, produtosAtivos, 1);

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
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return produtoRepository.findByIdAndOrganizationId(idProduto, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public Produto buscarProdutoAtivo(Integer idProduto) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return produtoRepository.findByIdAndOrganizationIdAndAtivoTrue(idProduto, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    // Mapping handled by ProdutoMapper

    private void validarLimiteProdutos(Plano plano, long produtosAtivos, int novosProdutos) {
        if (plano == null) {
            throw new EntityNotFoundException("Plano não encontrado");
        }
        Integer limite = plano.getMaxProdutos();
        if (limite != null && limite > 0 && produtosAtivos + novosProdutos > limite) {
            throw new IllegalStateException("Limite de produtos do plano atingido");
        }
    }
}
