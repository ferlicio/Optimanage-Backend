package com.AIT.Optimanage.Models.Payment;

import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.AuditableEntity;
import com.AIT.Optimanage.Models.OwnableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(OwnerEntityListener.class)
public class PaymentConfig extends AuditableEntity implements OwnableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    private String apiKey;
    private String clientId;
    private String clientSecret;
    private String environment;
}
