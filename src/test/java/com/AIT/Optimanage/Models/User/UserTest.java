package com.AIT.Optimanage.Models.User;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void isEnabledReturnsFalseWhenAtivoIsFalse() {
        User user = new User();
        user.setAtivo(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void isEnabledReturnsTrueWhenAtivoIsTrue() {
        User user = new User();
        user.setAtivo(true);
        assertTrue(user.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseWhenAtivoIsNull() {
        User user = new User();
        user.setAtivo(null);
        assertFalse(user.isEnabled());
    }
}
