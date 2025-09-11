package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClienteControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.AIT.Optimanage.Services.Cliente.ClienteService clienteService;

    @Test
    void whenCpfInvalid_thenReturnsBadRequest() throws Exception {
        String payload = "{\"atividadeId\":1,\"tipoPessoa\":\"FISICA\",\"origem\":\"SITE\",\"cpf\":\"123\"}";
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void whenCnpjInvalid_thenReturnsBadRequest() throws Exception {
        String payload = "{\"atividadeId\":1,\"tipoPessoa\":\"JURIDICA\",\"origem\":\"SITE\",\"cnpj\":\"123\"}";
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }
}
