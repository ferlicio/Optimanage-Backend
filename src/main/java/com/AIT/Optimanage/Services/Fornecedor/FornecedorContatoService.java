package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorContatoRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorContatoService {

    private final FornecedorContatoRepository fornecedorContatoRepository;
    private final FornecedorService fornecedorService;
    private final PlanoAccessGuard planoAccessGuard;

    public List<FornecedorContato> listarContatos(Integer idFornecedor) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return fornecedorContatoRepository.findAllByFornecedor_IdAndFornecedorOrganizationId(idFornecedor, organizationId);
    }

    public FornecedorContato listarUmContato(Integer idFornecedor, Integer idContato) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);
        return fornecedorContatoRepository.findByIdAndFornecedor_IdAndFornecedorOrganizationId(idContato, fornecedor.getId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public FornecedorContato cadastrarContato(Integer idFornecedor, FornecedorContato contato) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);

        planoAccessGuard.garantirPermissaoDeEscrita(fornecedor.getOrganizationId());

        contato.setId(null);
        contato.setFornecedor(fornecedor);
        contato.setTenantId(fornecedor.getOrganizationId());
        return fornecedorContatoRepository.save(contato);
    }

    public FornecedorContato editarContato(Integer idFornecedor, Integer idContato, FornecedorContato contato) {
        FornecedorContato contatoExistente = listarUmContato(idFornecedor, idContato);

        planoAccessGuard.garantirPermissaoDeEscrita(contatoExistente.getOrganizationId());

        contato.setId(contatoExistente.getId());
        contato.setFornecedor(contatoExistente.getFornecedor());
        contato.setTenantId(contatoExistente.getOrganizationId());
        return fornecedorContatoRepository.save(contato);
    }

    public void excluirContato(Integer idFornecedor, Integer idContato) {
        FornecedorContato contato = listarUmContato(idFornecedor, idContato);
        planoAccessGuard.garantirPermissaoDeEscrita(contato.getOrganizationId());
        fornecedorContatoRepository.delete(contato);
    }
}
