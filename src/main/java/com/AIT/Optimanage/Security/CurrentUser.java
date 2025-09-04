package com.AIT.Optimanage.Security;

import com.AIT.Optimanage.Models.User.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to retrieve the current authenticated {@link User}.
 * Allows setting a mocked user for tests via constructor or thread local.
 */
public class CurrentUser {

    private static final ThreadLocal<User> mockUser = new ThreadLocal<>();

    public static User get() {
        User user = mockUser.get();
        if (user != null) {
            return user;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    public CurrentUser(User user) {
        mockUser.set(user);
    }

    public static void set(User user) {
        mockUser.set(user);
    }

    public static void clear() {
        mockUser.remove();
    }
}
