package com.AIT.Optimanage.Controllers.User.dto;

import com.AIT.Optimanage.Models.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Integer id;
    private String nome;
    private String sobrenome;
    private String email;
    private Boolean ativo;
    private Role role;
}
