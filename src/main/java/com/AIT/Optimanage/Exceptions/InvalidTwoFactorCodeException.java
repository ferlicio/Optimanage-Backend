package com.AIT.Optimanage.Exceptions;

public class InvalidTwoFactorCodeException extends CustomRuntimeException {
    public InvalidTwoFactorCodeException() {
        super("Invalid 2FA code");
    }
}
