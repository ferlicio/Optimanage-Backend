package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Services.PlanoService;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private PlanoService planoService;

    private MeterRegistry meterRegistry;
    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        rateLimitingFilter = new RateLimitingFilter(meterRegistry, planoService, List.of("/auth/**"));
    }

    @AfterEach
    void clearBuckets() {
        Cache<?, ?> cache = (Cache<?, ?>) ReflectionTestUtils.getField(rateLimitingFilter, "buckets");
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Test
    void whenLimitExceeded_thenReturnsTooManyRequests() throws ServletException, IOException {
        String body = "{\"email\":\"user@example.com\",\"senha\":\"123456\"}";

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = newRequest(body);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = Mockito.mock(FilterChain.class);

            rateLimitingFilter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            verify(chain).doFilter(request, response);
        }

        Cache<String, ?> cache = getCache();
        assertThat(cache.asMap()).isNotEmpty();
        String cachedKey = cache.asMap().keySet().iterator().next();
        assertThat(cache.getIfPresent(cachedKey)).isNotNull();

        MockHttpServletRequest blockedRequest = newRequest(body);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        FilterChain blockedChain = Mockito.mock(FilterChain.class);

        rateLimitingFilter.doFilter(blockedRequest, blockedResponse, blockedChain);

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        verify(blockedChain, never()).doFilter(blockedRequest, blockedResponse);
    }

    private MockHttpServletRequest newRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/authenticate");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body.getBytes());
        request.setRemoteAddr("192.0.2.1");
        request.setServletPath("/auth/authenticate");
        return request;
    }

    @SuppressWarnings("unchecked")
    private Cache<String, ?> getCache() {
        return (Cache<String, ?>) ReflectionTestUtils.getField(rateLimitingFilter, "buckets");
    }
}
