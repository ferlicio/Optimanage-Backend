package com.AIT.Optimanage.Models.Payment;

import com.AIT.Optimanage.Models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "processed_payment_event",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "provider", "event_id"}))
public class ProcessedPaymentEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}
