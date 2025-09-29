package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceIntegrationResponse {

    private String marketplace;
    private String externalAccountId;
    private LocalDateTime connectedAt;
    private LocalDateTime lastSyncAt;
    private Boolean ativo;
}
