package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorEnderecoRepository;
import com.AIT.Optimanage.Security.CurrentUser;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorEnderecoService {

    private final FornecedorEnderecoRepository fornecedorEnderecoRepository;
    private final FornecedorService fornecedorService;

    public List<FornecedorEndereco> listarEnderecos(Integer idFornecedor) {
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return fornecedorEnderecoRepository.findAllByFornecedor_IdAndFornecedorOrganizationId(idFornecedor, organizationId);
    }

    public FornecedorEndereco listarUmEndereco(Integer idFornecedor, Integer idEndereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);
        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada");
        }
        return fornecedorEnderecoRepository.findByIdAndFornecedor_IdAndFornecedorOrganizationId(idEndereco, fornecedor.getId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public FornecedorEndereco cadastrarEndereco(Integer idFornecedor, FornecedorEndereco endereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);

        endereco.setId(null);
        endereco.setFornecedor(fornecedor);
        endereco.setTenantId(fornecedor.getOrganizationId());
        return fornecedorEnderecoRepository.save(endereco);
    }

    public FornecedorEndereco editarEndereco(Integer idFornecedor, Integer idEndereco, FornecedorEndereco endereco) {
        FornecedorEndereco enderecoExistente = listarUmEndereco(idFornecedor, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setFornecedor(enderecoExistente.getFornecedor());
        endereco.setTenantId(enderecoExistente.getOrganizationId());
        return fornecedorEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(Integer idFornecedor, Integer idEndereco) {
        FornecedorEndereco endereco = listarUmEndereco(idFornecedor, idEndereco);
        fornecedorEnderecoRepository.delete(endereco);
    }
}
