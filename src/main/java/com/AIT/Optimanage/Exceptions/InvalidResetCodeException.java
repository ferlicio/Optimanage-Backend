package com.AIT.Optimanage.Exceptions;

public class InvalidResetCodeException extends CustomRuntimeException {
    public InvalidResetCodeException() {
        super("Invalid reset code");
    }
}
