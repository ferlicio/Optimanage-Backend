package com.AIT.Optimanage.Security;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Support.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to retrieve the current authenticated {@link User}.
 * <p>
 * A {@link ThreadLocal} is used to allow tests to supply a mocked user
 * without relying on the Spring Security context.
 */
public final class CurrentUser {

    private static final ThreadLocal<User> mockUser = new ThreadLocal<>();

    private CurrentUser() {
        // utility class
    }

    public static User get() {
        User user = mockUser.get();
        if (user != null) {
            return user;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User authenticated) {
            return authenticated;
        }
        return null;
    }

    public static void set(User user) {
        mockUser.set(user);
    }

    public static void clear() {
        mockUser.remove();
    }

    public static Integer getOrganizationId() {
        User user = get();
        if (user != null) {
            return user.getTenantId();
        }
        return TenantContext.getTenantId();
    }
}
