package com.AIT.Optimanage.Auth;

import lombok.*;

@Setter
@Getter
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
