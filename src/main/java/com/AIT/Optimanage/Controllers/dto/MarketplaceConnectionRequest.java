package com.AIT.Optimanage.Controllers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceConnectionRequest {

    @NotBlank
    private String marketplace;

    @NotBlank
    private String externalAccountId;

    @NotBlank
    private String accessToken;
}
