package com.AIT.Optimanage.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String novaSenha;
}
