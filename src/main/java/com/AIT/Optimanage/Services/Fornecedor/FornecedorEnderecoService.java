package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorEndereco;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorEnderecoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorEnderecoService {

    private final FornecedorEnderecoRepository fornecedorEnderecoRepository;
    private final FornecedorService fornecedorService;

    public List<FornecedorEndereco> listarEnderecos(User loggedUser, Integer idFornecedor) {
        return fornecedorEnderecoRepository.findAllByFornecedor_IdAndFornecedorOwnerUser(idFornecedor, loggedUser);
    }

    public FornecedorEndereco listarUmEndereco(User loggedUser, Integer idFornecedor, Integer idEndereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(loggedUser, idFornecedor);
        return fornecedorEnderecoRepository.findByIdAndFornecedor_IdAndFornecedorOwnerUser(idEndereco, fornecedor.getId(), loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public FornecedorEndereco cadastrarEndereco(User loggedUser, Integer idFornecedor, FornecedorEndereco endereco) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(loggedUser, idFornecedor);

        endereco.setId(null);
        endereco.setFornecedor(fornecedor);
        return fornecedorEnderecoRepository.save(endereco);
    }

    public FornecedorEndereco editarEndereco(User loggedUser, Integer idFornecedor, Integer idEndereco, FornecedorEndereco endereco) {
        FornecedorEndereco enderecoExistente = listarUmEndereco(loggedUser, idFornecedor, idEndereco);

        endereco.setId(enderecoExistente.getId());
        endereco.setFornecedor(enderecoExistente.getFornecedor());
        return fornecedorEnderecoRepository.save(endereco);
    }

    public void excluirEndereco(User loggedUser, Integer idFornecedor, Integer idEndereco) {
        FornecedorEndereco endereco = listarUmEndereco(loggedUser, idFornecedor, idEndereco);
        fornecedorEnderecoRepository.delete(endereco);
    }
}
