package com.AIT.Optimanage.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class TwoFactorToggleRequest {
    @Email
    @NotBlank
    private String email;
    private boolean enable;
}
