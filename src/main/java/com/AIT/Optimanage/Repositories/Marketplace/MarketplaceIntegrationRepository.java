package com.AIT.Optimanage.Repositories.Marketplace;

import com.AIT.Optimanage.Models.Marketplace.MarketplaceIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketplaceIntegrationRepository extends JpaRepository<MarketplaceIntegration, Integer> {

    Optional<MarketplaceIntegration> findByOrganizationId(Integer organizationId);
}
