package com.AIT.Optimanage.Models;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import com.AIT.Optimanage.Models.Audit.AuditListener;
import com.AIT.Optimanage.Support.TenantEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditListener.class, TenantEntityListener.class})
@FilterDef(name = "organizationFilter", parameters = @ParamDef(name = "organizationId", type = Integer.class))
@Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Integer organizationId;

    // Temporary compatibility helpers for legacy tenant naming
    public Integer getTenantId() {
        return organizationId;
    }

    public void setTenantId(Integer tenantId) {
        this.organizationId = tenantId;
    }
}
