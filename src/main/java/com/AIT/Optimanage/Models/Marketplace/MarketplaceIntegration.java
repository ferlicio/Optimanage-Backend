package com.AIT.Optimanage.Models.Marketplace;

import com.AIT.Optimanage.Models.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "marketplace_integrations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_marketplace_integration_org", columnNames = "organization_id")
})
public class MarketplaceIntegration extends AuditableEntity {

    @Column(nullable = false)
    private String marketplace;

    @Column(name = "external_account_id")
    private String externalAccountId;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
