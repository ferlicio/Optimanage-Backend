package com.AIT.Optimanage.Support;

public class TenantContext {
    private static final ThreadLocal<Integer> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(Integer tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Integer getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
