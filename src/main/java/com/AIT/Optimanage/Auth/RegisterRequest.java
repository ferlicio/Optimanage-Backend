package com.AIT.Optimanage.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String sobrenome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

}
