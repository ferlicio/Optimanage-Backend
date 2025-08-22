package com.AIT.Optimanage.Auth;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nome;
    private String sobrenome;
    private String email;
    private String senha;

}
