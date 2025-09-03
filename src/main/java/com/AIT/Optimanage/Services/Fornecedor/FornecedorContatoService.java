package com.AIT.Optimanage.Services.Fornecedor;

import com.AIT.Optimanage.Models.Fornecedor.Fornecedor;
import com.AIT.Optimanage.Models.Fornecedor.FornecedorContato;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorContatoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorContatoService {

    private final FornecedorContatoRepository fornecedorContatoRepository;
    private final FornecedorService fornecedorService;

    public List<FornecedorContato> listarContatos(User loggedUser, Integer idFornecedor) {
        return fornecedorContatoRepository.findAllByFornecedor_IdAndFornecedorOwnerUser(idFornecedor, loggedUser);
    }

    public FornecedorContato listarUmContato(User loggedUser, Integer idFornecedor, Integer idContato) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(loggedUser, idFornecedor);
        return fornecedorContatoRepository.findByIdAndFornecedor_IdAndFornecedorOwnerUser(idContato, fornecedor.getId(), loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Contato não encontrado"));
    }

    public FornecedorContato cadastrarContato(User loggedUser, Integer idFornecedor, FornecedorContato contato) {
        Fornecedor fornecedor = fornecedorService.listarUmFornecedor(loggedUser, idFornecedor);

        contato.setId(null);
        contato.setFornecedor(fornecedor);
        return fornecedorContatoRepository.save(contato);
    }

    public FornecedorContato editarContato(User loggedUser, Integer idFornecedor, Integer idContato, FornecedorContato contato) {
        FornecedorContato contatoExistente = listarUmContato(loggedUser, idFornecedor, idContato);

        contato.setId(contatoExistente.getId());
        contato.setFornecedor(contatoExistente.getFornecedor());
        return fornecedorContatoRepository.save(contatoExistente);
    }

    public void excluirContato(User loggedUser, Integer idFornecedor, Integer idContato) {
        FornecedorContato contato = listarUmContato(loggedUser, idFornecedor, idContato);
        fornecedorContatoRepository.delete(contato);
    }
}
