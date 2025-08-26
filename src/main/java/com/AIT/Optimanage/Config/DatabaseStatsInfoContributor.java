package com.AIT.Optimanage.Config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.stereotype.Component;

import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;

@Component
public class DatabaseStatsInfoContributor implements InfoContributor {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public DatabaseStatsInfoContributor(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public void contribute(Builder builder) {
        builder.withDetail("clientes", clienteRepository.count())
               .withDetail("produtos", produtoRepository.count());
    }
}
