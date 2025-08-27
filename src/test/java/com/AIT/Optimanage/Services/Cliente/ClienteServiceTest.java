package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.User.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    @Test
    void validarClientePessoaFisicaDeveRemoverInscricaoMunicipal() {
        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setInscricaoMunicipal("12345");

        ClienteService service = new ClienteService(null);
        service.validarCliente(new User(), cliente);

        assertNull(cliente.getInscricaoMunicipal());
    }
}
