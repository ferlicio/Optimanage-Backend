package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Config.LocaleConfig;
import com.AIT.Optimanage.Exceptions.GlobalExceptionHandler;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, LocaleConfig.class})
class AuthenticationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        MDC.put("correlationId", "test-correlation-id");
    }

    @AfterEach
    void cleanup() {
        MDC.remove("correlationId");
    }

    @Test
    void whenRegisterRequestInvalid_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Falha na validação"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void whenAuthenticateRequestInvalid_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Falha na validação"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void whenLogout_thenReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
        verify(authenticationService).logout("token");
    }

    @Test
    void whenLogoutWithoutAuthorization_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest());
        verify(authenticationService, never()).logout(anyString());
    }

    @Test
    void whenLogoutWithMalformedAuthorization_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Token token"))
                .andExpect(status().isUnauthorized());
        verify(authenticationService, never()).logout(anyString());
    }
}
