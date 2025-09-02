package com.AIT.Optimanage.Support;

import com.AIT.Optimanage.Models.BaseEntity;
import jakarta.persistence.PrePersist;

public class TenantEntityListener {
    @PrePersist
    public void setTenant(BaseEntity entity) {
        Integer tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            entity.setTenantId(tenantId);
        }
    }
}
