package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Controllers.ExceptionHandler.GlobalExceptionHandler;
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

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthenticationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void whenRegisterRequestInvalid_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").exists());
    }

    @Test
    void whenAuthenticateRequestInvalid_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").exists());
    }
}
