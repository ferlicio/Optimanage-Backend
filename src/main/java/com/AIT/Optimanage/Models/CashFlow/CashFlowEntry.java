package com.AIT.Optimanage.Models.CashFlow;

import com.AIT.Optimanage.Models.AuditableEntity;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import com.AIT.Optimanage.Models.Audit.OwnerEntityListener;
import com.AIT.Optimanage.Models.OwnableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cash_flow_entries")
@EntityListeners(OwnerEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class CashFlowEntry extends AuditableEntity implements OwnableEntity {

    @Column(nullable = false, length = 128)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashFlowType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashFlowStatus status;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    public void ensureDefaultStatus() {
        if (status == null) {
            status = CashFlowStatus.ACTIVE;
        }
    }
}
