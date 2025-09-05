package com.AIT.Optimanage.Exceptions;

public class RefreshTokenNotFoundException extends CustomRuntimeException {
    public RefreshTokenNotFoundException() {
        super("Refresh token not found");
    }
}
