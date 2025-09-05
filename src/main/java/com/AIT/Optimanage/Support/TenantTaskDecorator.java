package com.AIT.Optimanage.Support;

import org.springframework.core.task.TaskDecorator;

/**
 * Propagates the current {@link TenantContext} to async threads.
 */
public class TenantTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Integer tenantId = TenantContext.getTenantId();
        return () -> {
            try {
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                }
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}

