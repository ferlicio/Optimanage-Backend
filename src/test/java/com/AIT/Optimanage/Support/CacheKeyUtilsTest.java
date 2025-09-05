package com.AIT.Optimanage.Support;

import com.AIT.Optimanage.Security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheKeyUtilsTest {

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
        TenantContext.clear();
    }

    @Test
    void shouldUseTenantWhenUserIsNull() {
        TenantContext.setTenantId(42);
        String key = CacheKeyUtils.generateKey("test");
        assertTrue(key.startsWith("42-"));
    }

    @Test
    void shouldFallbackToDefaultWhenNoUserOrTenant() {
        String key = CacheKeyUtils.generateKey("test");
        assertTrue(key.startsWith("default-"));
    }
}
