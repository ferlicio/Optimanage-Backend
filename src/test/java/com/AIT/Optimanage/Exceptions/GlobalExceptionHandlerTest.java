package com.AIT.Optimanage.Exceptions;

import com.AIT.Optimanage.Config.LocaleConfig;
import com.AIT.Optimanage.Exceptions.CustomRuntimeException;
import com.AIT.Optimanage.Exceptions.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.AIT.Optimanage.Config.JwtAuthenticationFilter;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Config.TenantFilter;
import com.AIT.Optimanage.Auth.TokenBlacklistService;
import com.AIT.Optimanage.Services.PlanoService;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@WebMvcTest(
        controllers = GlobalExceptionHandlerTest.TestController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, LocaleConfig.class})
public class GlobalExceptionHandlerTest {

    @TestConfiguration
    static class MetricsConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private PlanoService planoService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @RestController
    public static class TestController {
        @GetMapping("/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Forbidden");
        }

        @GetMapping("/runtime")
        public void runtime() {
            throw new CustomRuntimeException("Custom error");
        }

        @PostMapping("/validate")
        public void validate(@RequestBody @Valid DummyRequest request) {
        }
    }

    static class DummyRequest {
        @NotBlank
        public String name;
    }

    @BeforeEach
    void setup() {
        MDC.put("correlationId", "test-correlation-id");
    }

    @AfterEach
    void cleanup() {
        MDC.remove("correlationId");
    }

    @Test
    void whenValidationFails_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/validate")
                .header("Accept-Language", "pt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void whenAccessDenied_thenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Forbidden"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void whenCustomRuntime_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/runtime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Custom error"))
                .andExpect(jsonPath("$.correlationId").exists());
    }
}
