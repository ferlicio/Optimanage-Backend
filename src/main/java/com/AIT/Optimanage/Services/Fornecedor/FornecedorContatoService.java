package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorContatoRepository;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import com.AIT.Optimanage.Security.CurrentUser;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorContatoService {

    private final FornecedorContatoRepository fornecedorContatoRepository;
    private final FornecedorService fornecedorService;

    public List<FornecedorContato> listarContatos(Integer idFornecedor) {
        User loggedUser = CurrentUser.get();
        return fornecedorContatoRepository.findAllByFornecedor_IdAndFornecedorOwnerUser(idFornecedor, loggedUser);
    }

    public FornecedorContato listarUmContato(Integer idFornecedor, Integer idContato) {
        User loggedUser = CurrentUser.get();
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);
        return fornecedorContatoRepository.findByIdAndFornecedor_IdAndFornecedorOwnerUser(idContato, fornecedor.getId(), loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public FornecedorContato cadastrarContato(Integer idFornecedor, FornecedorContato contato) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(idFornecedor);

        contato.setId(null);
        contato.setFornecedor(fornecedor);
        return fornecedorContatoRepository.save(contato);
    }

    public FornecedorContato editarContato(Integer idFornecedor, Integer idContato, FornecedorContato contato) {
        FornecedorContato contatoExistente = listarUmContato(idFornecedor, idContato);

        contato.setId(contatoExistente.getId());
        contato.setFornecedor(contatoExistente.getFornecedor());
        return fornecedorContatoRepository.save(contatoExistente);
    }

    public void excluirContato(Integer idFornecedor, Integer idContato) {
        FornecedorContato contato = listarUmContato(idFornecedor, idContato);
        fornecedorContatoRepository.delete(contato);
    }
}
