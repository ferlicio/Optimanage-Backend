package com.AIT.Optimanage.Controllers.dto;

import com.AIT.Optimanage.Models.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInviteResponse {
    private String code;
    private Role role;
    private Instant expiresAt;
    private boolean used;
    private String email;
}
