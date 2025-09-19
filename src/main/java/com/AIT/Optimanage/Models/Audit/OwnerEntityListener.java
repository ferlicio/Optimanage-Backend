package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.OwnableEntity;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class OwnerEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        ensureOrganization(entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        ensureOrganization(entity);
    }

    private void ensureOrganization(Object entity) {
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOrganizationId() == null) {
                Integer organizationId = CurrentUser.getOrganizationId();
                if (organizationId != null) {
                    ownable.setOrganizationId(organizationId);
                }
            }
        }
    }
}
