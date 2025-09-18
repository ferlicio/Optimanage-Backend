package com.AIT.Optimanage.Repositories.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentConfig;
import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentConfigRepository extends JpaRepository<PaymentConfig, Integer> {
    Optional<PaymentConfig> findByOrganizationIdAndProvider(Integer organizationId, PaymentProvider provider);
}
