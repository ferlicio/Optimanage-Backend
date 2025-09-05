package com.AIT.Optimanage.Exceptions;

public class UserNotFoundException extends CustomRuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}
