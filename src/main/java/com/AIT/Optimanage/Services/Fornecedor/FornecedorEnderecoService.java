package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Models.User.User;
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
        User loggedUser = CurrentUser.get();
        return fornecedorEnderecoRepository.findAllByFornecedor_IdAndFornecedorOwnerUser(idFornecedor, loggedUser);
    }

    public FornecedorEndereco listarUmEndereco(Integer idFornecedor, Integer idEndereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);
        return fornecedorEnderecoRepository.findByIdAndFornecedor_IdAndFornecedorOwnerUser(idEndereco, fornecedor.getId(), CurrentUser.get())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public FornecedorEndereco cadastrarEndereco(Integer idFornecedor, FornecedorEndereco endereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);

        endereco.setId(null);
        endereco.setFornecedor(fornecedor);
        return fornecedorEnderecoRepository.save(endereco);
    }

    public FornecedorEndereco editarEndereco(Integer idFornecedor, Integer idEndereco, FornecedorEndereco endereco) {
        FornecedorEndereco enderecoExistente = listarUmEndereco(idFornecedor, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setFornecedor(enderecoExistente.getFornecedor());
        return fornecedorEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(Integer idFornecedor, Integer idEndereco) {
        FornecedorEndereco endereco = listarUmEndereco(idFornecedor, idEndereco);
        fornecedorEnderecoRepository.delete(endereco);
    }
}
