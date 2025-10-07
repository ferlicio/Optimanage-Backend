package com.AIT.Optimanage.Services.Cliente;

import com.AIT.Optimanage.Models.Cliente.Cliente;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    @Test
    void validarClientePessoaFisicaDeveRemoverInscricaoMunicipal() {
        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setInscricaoMunicipal("12345");

        ClienteService service = new ClienteService(null, null, null, null, null);
        service.validarCliente(cliente);

        assertNull(cliente.getInscricaoMunicipal());
    }
}
