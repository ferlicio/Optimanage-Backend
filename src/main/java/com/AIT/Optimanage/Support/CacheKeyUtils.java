package com.AIT.Optimanage.Support;

import com.AIT.Optimanage.Security.CurrentUser;

/**
 * Utility to generate cache keys that consider the current user or tenant.
 */
public final class CacheKeyUtils {

    private CacheKeyUtils() {
        // utility class
    }

    public static String generateKey(Object search) {
        return getIdentifier() + "-" + (search != null ? search.hashCode() : 0);
    }

    public static String generateKey() {
        return getIdentifier();
    }

    private static String getIdentifier() {
        var user = CurrentUser.get();
        if (user != null && user.getId() != null) {
            return String.valueOf(user.getId());
        }
        Integer tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return String.valueOf(tenantId);
        }
        return "default";
    }
}
