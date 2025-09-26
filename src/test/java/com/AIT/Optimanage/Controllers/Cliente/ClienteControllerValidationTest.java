package com.AIT.Optimanage.Controllers.Cliente;

import com.AIT.Optimanage.Auth.TokenBlacklistService;
import com.AIT.Optimanage.Config.JwtAuthenticationFilter;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Config.LocaleConfig;
import com.AIT.Optimanage.Config.RateLimitingFilter;
import com.AIT.Optimanage.Config.TenantFilter;
import com.AIT.Optimanage.Exceptions.GlobalExceptionHandler;
import com.AIT.Optimanage.Services.PlanoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.slf4j.MDC;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, LocaleConfig.class})
class ClienteControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.AIT.Optimanage.Services.Cliente.ClienteService clienteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private PlanoService planoService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup() {
        MDC.put("correlationId", "test-correlation-id");
    }

    @AfterEach
    void cleanup() {
        MDC.remove("correlationId");
    }

    @Test
    void whenCpfInvalid_thenReturnsBadRequest() throws Exception {
        String payload = "{\"atividadeId\":1,\"tipoPessoa\":\"PF\",\"origem\":\"SITE\",\"cpf\":\"123\"}";
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
        String payload = "{\"atividadeId\":1,\"tipoPessoa\":\"PJ\",\"origem\":\"SITE\",\"cnpj\":\"123\"}";
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }
}
