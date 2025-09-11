package com.AIT.Optimanage.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorSetupResponse {
    private String qrCodeUrl;
}

