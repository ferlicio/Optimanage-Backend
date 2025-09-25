package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a domain action that must be tracked for compliance/auditability purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_trail")
public class AuditTrail extends AuditableEntity {

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    @Column(name = "action", nullable = false, length = 150)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
