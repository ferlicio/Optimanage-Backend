package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Models.Atividade;
import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.Search.FornecedorSearch;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
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

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Cacheable(value = "fornecedores", key = "#loggedUser.id + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<Fornecedor> listarFornecedores(User loggedUser, FornecedorSearch pesquisa) {
        // Configuração de paginação e ordenação
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");

        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));

        // Realiza a busca no repositório com os filtros definidos e associando o usuário logado
        return fornecedorRepository.buscarFornecedores(
                loggedUser.getId(),
                pesquisa.getId(),
                pesquisa.getNome(),
                pesquisa.getCpfOuCnpj(),
                pesquisa.getAtividade(),
                pesquisa.getEstado(),
                pesquisa.getTipoPessoa(),
                pesquisa.getAtivo() != null ? pesquisa.getAtivo() : true,
                pageable
        );
    }

    public Fornecedor listarUmFornecedor(User loggedUser, Integer idFornecedor) {
        return fornecedorRepository.findByIdAndOwnerUserAndAtivoTrue(idFornecedor, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public Fornecedor criarFornecedor(User loggedUser, FornecedorRequest request) {
        Fornecedor fornecedor = fromRequest(request);
        fornecedor.setDataCadastro(LocalDate.now());
        validarFornecedor(loggedUser, fornecedor);
        return fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public Fornecedor editarFornecedor(User loggedUser, Integer idFornecedor, FornecedorRequest request) {
        Fornecedor fornecedorSalvo = listarUmFornecedor(loggedUser, idFornecedor);
        Fornecedor fornecedor = fromRequest(request);
        fornecedor.setId(fornecedorSalvo.getId());
        fornecedor.setDataCadastro(fornecedorSalvo.getDataCadastro());
        validarFornecedor(loggedUser, fornecedor);
        return fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void inativarFornecedor(User loggedUser, Integer idFornecedor) {
        Fornecedor fornecedor = listarUmFornecedor(loggedUser, idFornecedor);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public Fornecedor reativarFornecedor(User loggedUser, Integer idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUser(idFornecedor, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedor.setAtivo(true);
        return fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void removerFornecedor(User loggedUser, Integer idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUser(idFornecedor, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedorRepository.delete(fornecedor);
    }

    public void validarFornecedor(User loggedUser, Fornecedor fornecedor) {
        if (fornecedor.getTipoPessoa() == TipoPessoa.PF) {
            fornecedor.setNomeFantasia(null);
            fornecedor.setRazaoSocial(null);
            fornecedor.setCnpj(null);
            fornecedor.setInscricaoEstadual(null);
            fornecedor.setInscricaoMunicipal(null);
        } else {
            if (fornecedor.getCnpj() == null || fornecedor.getCnpj().isEmpty()) {
                throw new IllegalArgumentException("CNPJ é obrigatório");
            }
            fornecedor.setNome(null);
            fornecedor.setCpf(null);
        }

    }

    private Fornecedor fromRequest(FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor();
        Atividade atividade = new Atividade();
        atividade.setId(request.getAtividadeId());
        fornecedor.setAtividade(atividade);
        fornecedor.setTipoPessoa(request.getTipoPessoa());
        fornecedor.setOrigem(request.getOrigem());
        fornecedor.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
        fornecedor.setNome(request.getNome());
        fornecedor.setNomeFantasia(request.getNomeFantasia());
        fornecedor.setRazaoSocial(request.getRazaoSocial());
        fornecedor.setCpf(request.getCpf());
        fornecedor.setCnpj(request.getCnpj());
        fornecedor.setInscricaoEstadual(request.getInscricaoEstadual());
        fornecedor.setInscricaoMunicipal(request.getInscricaoMunicipal());
        fornecedor.setSite(request.getSite());
        fornecedor.setInformacoesAdicionais(request.getInformacoesAdicionais());
        return fornecedor;
    }
}
