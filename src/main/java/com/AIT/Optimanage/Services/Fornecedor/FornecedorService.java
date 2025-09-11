package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Controllers.dto.FornecedorResponse;
import com.AIT.Optimanage.Mappers.FornecedorMapper;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.Search.FornecedorSearch;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import com.AIT.Optimanage.Security.CurrentUser;
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
    private final FornecedorMapper fornecedorMapper;

    @Cacheable(value = "fornecedores", key = "T(com.AIT.Optimanage.Security.CurrentUser).get().getId() + '-' + #pesquisa.hashCode()")
    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listarFornecedores(FornecedorSearch pesquisa) {
        User loggedUser = CurrentUser.get();
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
        ).map(fornecedorMapper::toResponse);
    }

    public FornecedorResponse listarUmFornecedor(Integer idFornecedor) {
        User loggedUser = CurrentUser.get();
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUserAndAtivoTrue(idFornecedor, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        return fornecedorMapper.toResponse(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse criarFornecedor(FornecedorRequest request) {
        User loggedUser = CurrentUser.get();
        Fornecedor fornecedor = fornecedorMapper.toEntity(request);
        fornecedor.setOwnerUser(loggedUser);
        fornecedor.setDataCadastro(LocalDate.now());
        validarFornecedor(fornecedor);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse editarFornecedor(Integer idFornecedor, FornecedorRequest request) {
        Fornecedor fornecedorSalvo = fornecedorRepository.findByIdAndOwnerUserAndAtivoTrue(idFornecedor, CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        Fornecedor fornecedor = fornecedorMapper.toEntity(request);
        fornecedor.setId(fornecedorSalvo.getId());
        fornecedor.setDataCadastro(fornecedorSalvo.getDataCadastro());
        validarFornecedor(fornecedor);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void inativarFornecedor(Integer idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUserAndAtivoTrue(idFornecedor, CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public FornecedorResponse reativarFornecedor(Integer idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUserAndAtivoFalse(idFornecedor, CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedor.setAtivo(true);
        return fornecedorMapper.toResponse(fornecedorRepository.save(fornecedor));
    }

    @CacheEvict(value = "fornecedores", allEntries = true)
    public void removerFornecedor(Integer idFornecedor) {
        User loggedUser = CurrentUser.get();
        Fornecedor fornecedor = fornecedorRepository.findByIdAndOwnerUser(idFornecedor, loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado"));
        fornecedorRepository.delete(fornecedor);
    }

    public void validarFornecedor(Fornecedor fornecedor) {
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
}
