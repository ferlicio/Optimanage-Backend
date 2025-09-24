package com.AIT.Optimanage.Support;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;

import java.util.Objects;
import java.util.Optional;
public final class CacheKeyResolver {

    private CacheKeyResolver() {
    }

    public static String userScopedKey(Object... segments) {
        Integer userId = Optional.ofNullable(CurrentUser.get())
                .map(User::getId)
                .orElse(null);
        return compose("user", userId, segments);
    }

    public static String userScopedKey(Object fallbackUser, Object... segments) {
        Integer userId = Optional.ofNullable(CurrentUser.get())
                .map(User::getId)
                .orElseGet(() -> extractUserId(fallbackUser));
        return compose("user", userId, segments);
    }

    public static String organizationScopedKey(Object... segments) {
        Integer organizationId = CurrentUser.getOrganizationId();
        return compose("org", organizationId, segments);
    }

    private static String compose(String scope, Object scopeId, Object... segments) {
        String base = scope + ':' + Objects.toString(scopeId, "none");
        if (segments == null || segments.length == 0) {
            return base;
        }
        int hash = Objects.hash((Object[]) segments);
        return base + ':' + Integer.toHexString(hash);
    }

    private static Integer extractUserId(Object candidate) {
        if (candidate instanceof User user) {
            return user.getId();
        }
        if (candidate instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
