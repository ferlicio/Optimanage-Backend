package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Auth.AuthenticationController;
import com.AIT.Optimanage.Auth.AuthenticationRequest;
import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Auth.AuthenticationService;
import com.AIT.Optimanage.Services.PlanoService;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import({RateLimitingFilter.class, RateLimitingFilterTest.TestConfig.class})
class RateLimitingFilterTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PlanoService planoService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(rateLimitingFilter)
                .build();

        when(authenticationService.authenticate(ArgumentMatchers.any(AuthenticationRequest.class)))
                .thenReturn(AuthenticationResponse.builder().token("t").refreshToken("r").build());
    }

    @AfterEach
    void clearBuckets() {
        Cache<?, ?> cache = (Cache<?, ?>) ReflectionTestUtils.getField(rateLimitingFilter, "buckets");
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Test
    void whenLimitExceeded_thenReturnsTooManyRequests() throws Exception {
        String body = "{\"email\":\"user@example.com\",\"senha\":\"123456\"}";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}

