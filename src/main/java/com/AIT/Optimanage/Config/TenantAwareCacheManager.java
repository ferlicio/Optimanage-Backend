package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Support.TenantContext;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Optional;

public class TenantAwareCacheManager extends CaffeineCacheManager {
    @Override
    public Cache getCache(String name) {
        String tenant = Optional.ofNullable(TenantContext.getTenantId())
                .map(String::valueOf)
                .orElse("default");
        return super.getCache(name + "::" + tenant);
    }
}
