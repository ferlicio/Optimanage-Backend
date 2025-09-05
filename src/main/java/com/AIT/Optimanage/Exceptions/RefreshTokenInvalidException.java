package com.AIT.Optimanage.Exceptions;

public class RefreshTokenInvalidException extends CustomRuntimeException {
    public RefreshTokenInvalidException() {
        super("Refresh token invalid");
    }
}
