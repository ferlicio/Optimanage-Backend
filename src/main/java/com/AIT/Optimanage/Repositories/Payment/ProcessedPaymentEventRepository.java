package com.AIT.Optimanage.Repositories.Payment;

import com.AIT.Optimanage.Models.Payment.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedPaymentEventRepository extends JpaRepository<ProcessedPaymentEvent, Integer> {
}
