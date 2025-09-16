package com.AIT.Optimanage.Repositories.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentProvider;
import com.AIT.Optimanage.Models.Payment.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedPaymentEventRepository extends JpaRepository<ProcessedPaymentEvent, Integer> {
    boolean existsByProviderAndEventIdAndOrganizationId(PaymentProvider provider, String eventId, Integer organizationId);
}
