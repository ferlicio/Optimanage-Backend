package com.AIT.Optimanage.Controllers.dto;

import com.AIT.Optimanage.Models.User.Role;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInviteRequest {
    @NotNull
    private Role role;

    @NotNull
    @Future
    private Instant expiresAt;

    private String email;
}
